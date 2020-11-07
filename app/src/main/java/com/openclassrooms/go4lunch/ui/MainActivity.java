package com.openclassrooms.go4lunch.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.ActivityMainBinding;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.callback.Callback;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.PermissionRequest;

import static com.openclassrooms.go4lunch.utils.Constante.RC_SIGN_IN;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int RC_CAMERA_AND_LOCATION = 123;
    private ActivityMainBinding binding;
    private Handler handler;
    private String[] perms = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        EasyPermissions.requestPermissions(
                new PermissionRequest.Builder(this, RC_CAMERA_AND_LOCATION, perms)
                        .setRationale(R.string.camera_and_location_rationale)
                        .setPositiveButtonText(R.string.rationale_ask_ok)
                        .setNegativeButtonText(R.string.rationale_ask_cancel)
                        .build());

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
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.TwitterBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
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
                Log.d("connection", "Connexion");
                startActivity(new Intent(this, ActivityWithFrag.class));
            } else {
                Log.d("connection", "" + response.getError().getErrorCode());
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {

        handler = new Handler();
        Runnable runnable = () -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                startActivity(new Intent(this, ActivityWithFrag.class));
            }else {
                connectFirebase();
            }
        };

        handler.postDelayed(runnable, 3000);
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
            finish();
    }
}