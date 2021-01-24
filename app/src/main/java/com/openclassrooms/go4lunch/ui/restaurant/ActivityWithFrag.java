package com.openclassrooms.go4lunch.ui.restaurant;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.ActivityWithFragBinding;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.models.details.Period;
import com.openclassrooms.go4lunch.ui.restaurant.maps.MapsFragment;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ActivityWithFrag extends AppCompatActivity{

    @Inject
    public DatabaseReference refUsers;
    @Inject
    public Location location;
    private ActivityWithFragBinding binding;
    private RestaurantViewModel restaurantViewModel;
    private Context context = this;
    private FirebaseUser firebaseUser;
    private User user;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithFragBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Query query = refUsers.child(firebaseUser.getUid());

        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                //setMarkers()
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        restaurantViewModel.setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));



        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navDrawer = navHostFragment.getNavController();
        NavigationView navView = findViewById(R.id.navBar);
        NavigationUI.setupWithNavController(navView, navDrawer);

        View headerView = navView.getHeaderView(0);
        ImageView imgUser = headerView.findViewById(R.id.imgUser);
        TextView userName = headerView.findViewById(R.id.userName);

        Glide.with(this).load(firebaseUser.getPhotoUrl())
                .apply(new RequestOptions()
                .circleCrop()
                .format(DecodeFormat.PREFER_ARGB_8888)
                .override(Target.SIZE_ORIGINAL))
                .into(imgUser);
        userName.setText(firebaseUser.getDisplayName());

    }

    public void openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

    public void openBottomSheetDialog(Restaurant restaurant, String fragment) {
        Log.d("marker", "bottomsheetdialog");
        Log.d("listToHashMap", "" + restaurant.getOpeningHours());
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        if (fragment.equals("list")) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            binding.collapsedBottom.setVisibility(View.GONE);
            binding.expandedBottom.setVisibility(View.VISIBLE);
        } else {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            binding.collapsedBottom.setVisibility(View.VISIBLE);
            binding.expandedBottom.setVisibility(View.GONE);
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                Log.d("newState", "" + newState);
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    binding.collapsedBottom.setVisibility(View.GONE);
                    binding.expandedBottom.setVisibility(View.VISIBLE);

                } else if(newState == BottomSheetBehavior.STATE_COLLAPSED){
                    binding.collapsedBottom.setVisibility(View.VISIBLE);
                    binding.expandedBottom.setVisibility(View.GONE);

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                if (slideOffset > 0.5){
                    binding.collapsedBottom.setVisibility(View.GONE);
                    binding.expandedBottom.setVisibility(View.VISIBLE);
                } else {
                    binding.collapsedBottom.setVisibility(View.VISIBLE);
                    binding.expandedBottom.setVisibility(View.GONE);
                }
            }
        });

        bottomSheetBehavior.setPeekHeight(650);
        collapsedBottom(restaurant);
        expandedBottom(restaurant);

        binding.bottomSheet.setVisibility(View.VISIBLE);
    }

    private void collapsedBottom(Restaurant restaurant) {
        binding.picRestau.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), (int) (view.getHeight() + 40F), 40F);
            }
        });
        binding.picRestau.setClipToOutline(true);

        binding.nameRestau.setText(restaurant.getName());
        binding.adress.setText(restaurant.getVicinity());
        restaurant.getActualStatus();
        if (restaurant.getOpeningHours().getOpenNow() && getTodayExist(restaurant)) {
            Log.d("horaireRestau", " KOINKOINKOIN ");
        }
        Log.d("horaireRestau", " " + restaurant.getOpeningHours().getPeriods().get(3));
        if (!restaurant.getOpeningHours().getOpenNow()) {
            binding.chipHours.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
            binding.chipHours.setText("Close");
        } else {
            binding.chipHours.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
            binding.chipHours.setText("Open");
        }
        Glide.with(context).load(restaurant.getPicUrl()).centerCrop().into(binding.picRestau);

        binding.chipDistance.setText("" + restaurant.getDistance() + " m");
        Log.d("téléphone", "" + restaurant.getPhoneNumber());

        binding.imgPhone.setOnClickListener(v -> dialPhone(restaurant.getPhoneNumber()));
    }

    private void expandedBottom(Restaurant restaurant) {
        Log.d("dayOfWeek", "" + restaurant.getPlaceId());
        binding.textNameRestau.setText(restaurant.getName());
        binding.textAdressRestau.setText(restaurant.getVicinity());
        Glide.with(context).load(restaurant.getPicUrl()).centerCrop().into(binding.restauImg);
        binding.chipsDistanceExpanded.setText("" + restaurant.getDistance()+ " m");
        binding.phoneImage.setOnClickListener(v -> dialPhone(restaurant.getPhoneNumber()));

        if (user.isRestauChoosen() && user.getThisDayRestau().getPlaceId() == restaurant.getPlaceId()) {
            binding.fabEatButton.setImageResource(R.drawable.ic_baseline_check_circle_24);
        } else {
            binding.fabEatButton.setImageResource(R.drawable.ic_outline_check_circle_24);
        }

        if (user.getFavRestau() != null && containsRestaurant(user.getFavRestau(), restaurant)) {
            binding.likeImage.setImageResource(R.drawable.ic_baseline_star_rate_24);
        } else {
            binding.likeImage.setImageResource(R.drawable.ic_outline_star_rate_24);
        }


        binding.likeImage.setOnClickListener(v -> {
            HashMap<String, Object> restauMap = new HashMap<>();
            if (user.getFavRestau() != null && containsRestaurant(user.getFavRestau(), restaurant)) {
                Log.d("userFavRestau", "Before " + user.getFavRestau().size());
                user.getFavRestau().removeIf(restaurant1 -> restaurant1.getPlaceId().equals(restaurant.getPlaceId()));
                Log.d("userFavRestau", "After " + user.getFavRestau().size());
                restauMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(),user.getFavRestau(),user.getThisDayRestau(), user.isRestauChoosen()));
                refUsers.updateChildren(restauMap);
                binding.likeImage.setImageResource(R.drawable.ic_outline_star_rate_24);
            } else {
                user.getFavRestau().add(restaurant);
                restauMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(),user.getThisDayRestau(), user.isRestauChoosen()));
                refUsers.updateChildren(restauMap);

                binding.likeImage.setImageResource(R.drawable.ic_baseline_star_rate_24);
            }
        });

        binding.fabEatButton.setOnClickListener(v -> {
            HashMap<String, Object> restauMap = new HashMap<>();
            if (user.isRestauChoosen() && user.getThisDayRestau().getPlaceId().equals(restaurant.getPlaceId())) {
                restauMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(),null, false));
                refUsers.updateChildren(restauMap);
                user.setRestauChoosen(false);
                binding.fabEatButton.setImageResource(R.drawable.ic_outline_check_circle_24);
            } else if (user.isRestauChoosen() && !user.getThisDayRestau().getPlaceId().equals(restaurant.getPlaceId())){
                restauMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(), restaurant, true));
                refUsers.updateChildren(restauMap);
                user.setRestauChoosen(true);
                user.setThisDayRestau(restaurant);
                binding.fabEatButton.setImageResource(R.drawable.ic_baseline_check_circle_24);
            } else {
                restauMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(), restaurant, true));
                refUsers.updateChildren(restauMap);
                user.setRestauChoosen(true);
                user.setThisDayRestau(restaurant);
                binding.fabEatButton.setImageResource(R.drawable.ic_baseline_check_circle_24);
            }
            Log.d("firebaseTest", "" + user.getThisDayRestau().getPlaceId());
        });



        if (restaurant.getWebsite() == null) binding.websiteImage.setEnabled(false);
        binding.websiteImage.setOnClickListener(v -> {
            Uri webpage = Uri.parse(restaurant.getWebsite());
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
    }



    private void dialPhone(String phoneNumber) {
        Log.d("téléphone 2", "" + phoneNumber);
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (getCurrentFocus() != null)
        Log.d("currentFocus", "" + getCurrentFocus().getId());
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomSheet);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){

                Rect outRect = new Rect();
                binding.bottomSheet.getGlobalVisibleRect(outRect);

                if(!outRect.contains((int)event.getRawX(), (int)event.getRawY()))
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            }
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public void onBackPressed() {
        if (binding.bottomSheet.getVisibility() == View.VISIBLE) {
            binding.bottomSheet.setVisibility(View.GONE);
        }else {
            super.onBackPressed();
        }
    }

    public boolean containsRestaurant(final List<Restaurant> rest, Restaurant restaurant) {
        boolean contains = false;
        for (Restaurant item : rest) {
            if (item.getPlaceId().equals(restaurant.getPlaceId())) {
                Log.d("trueResult", "true but fail");
                contains = true;
            }
        }
        return contains;
    }

    private boolean getTodayExist(Restaurant restaurant) {
        boolean exist = false;
        for (Period period : restaurant.getOpeningHours().getPeriods()) {
            Log.d("horaireRestau", " " + period.getClose().getDay());
            if (period.getClose().getDay() == 0){
                Log.d("horaireRestau", " céchelou ");
                exist = true;
            }
        }
        return exist;
    }

}