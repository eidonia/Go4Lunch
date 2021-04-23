package com.openclassrooms.go4lunch.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.ActivityMainBinding;
import com.openclassrooms.go4lunch.event.ActivityFragEvent;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.utils.ConnectionService;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;
import com.openclassrooms.go4lunch.worker.NotifRestau;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static com.openclassrooms.go4lunch.utils.Constante.LOG_IN_EJABBERD;
import static com.openclassrooms.go4lunch.utils.Constante.RC_SIGN_IN;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, LocationListener {

    @Inject
    public StorageReference storage;
    private static final int RC_CAMERA_AND_LOCATION = 123;
    private RestaurantViewModel restaurantViewModel;
    private ActivityMainBinding binding;
    private Handler handler;
    private final String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.RECORD_AUDIO};
    private Location location;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            EasyPermissions.requestPermissions(
                    new PermissionRequest.Builder(this, RC_CAMERA_AND_LOCATION, perms)
                            .setRationale(R.string.camera_and_location_rationale)
                            .setPositiveButtonText(R.string.rationale_ask_ok)
                            .setNegativeButtonText(R.string.rationale_ask_cancel)
                            .build());
        } else {
            createUi();
        }

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.openclassrooms.go4lunch",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }


        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.openclassrooms.go4lunch",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

    private void connectFirebase() {

        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.FacebookBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setLogo(R.drawable.meal_v3_final)
                        .build(),
                RC_SIGN_IN
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent = new Intent(this, ConnectionService.class);
                startService(intent);
                Log.e("jsonUSer", user.getDisplayName() + " " + user.getPhotoUrl() + " " + user.getEmail());
                Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    if (user.getPhotoUrl() == null) {
                        restaurantViewModel.addUser(user.getDisplayName(), user.getEmail(), null);
                    } else {
                        restaurantViewModel.addUser(user.getDisplayName(), user.getEmail(), user.getPhotoUrl());
                    }
                }, 3000);

            } else {
                Log.e("connectionFirebase", "" + response.getError().getErrorCode());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        createUi();
    }

    @SuppressLint("MissingPermission")
    private void createUi() {
        try {
            NotifRestau.startWorker(this);
        } catch (ParseException e) {
            Log.e("testHourNotif", "error : " + e.getMessage());
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 30, this);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(provider);
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        restaurantViewModel.setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        handler = new Handler();
        Runnable runnable = () -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                Intent intent = new Intent(this, ConnectionService.class);
                startService(intent);
                restaurantViewModel.getUser().observe(this, user1 -> {
                    Log.d("json", "user : " + user1.getName());
                    prefs.edit()
                            .putString("xmpp_jid", user1.getEjabberdName())
                            .apply();

                    Intent sendIntent = new Intent();
                    sendIntent.setAction(LOG_IN_EJABBERD);
                    sendIntent.putExtra("userJid", user1.getEjabberdName());
                    sendIntent.putExtra("psswdJid", user1.getEjabberdPsswd());
                    sendBroadcast(sendIntent);
                });
                startActivity(new Intent(this, ActivityWithFrag.class));
            } else {
                prefs.edit()
                        .putBoolean("isNotifActiv", true)
                        .apply();

                connectFirebase();
            }
        };

        handler.postDelayed(runnable, 3000);
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        finish();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    @Subscribe
    public void onLauchActivityFrag(ActivityFragEvent event) {
        restaurantViewModel.getUser().observe(this, user1 -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(LOG_IN_EJABBERD);
            sendIntent.putExtra("userJid", user1.getEjabberdName());
            sendIntent.putExtra("psswdJid", user1.getEjabberdPsswd());
            sendBroadcast(sendIntent);
            startActivity(new Intent(MainActivity.this, ActivityWithFrag.class));
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }
}