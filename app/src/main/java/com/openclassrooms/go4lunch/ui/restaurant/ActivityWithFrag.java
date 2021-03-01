package com.openclassrooms.go4lunch.ui.restaurant;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Outline;
import android.graphics.Rect;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.ActivityWithFragBinding;
import com.openclassrooms.go4lunch.event.RefreshMarkers;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.models.details.Period;
import com.openclassrooms.go4lunch.ui.restaurant.workmates.UserListAdapter;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;
import com.openclassrooms.go4lunch.worker.NotifRestau;

import org.greenrobot.eventbus.EventBus;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import static com.openclassrooms.go4lunch.utils.Constante.ADD_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.BSD_ADA;
import static com.openclassrooms.go4lunch.utils.Constante.CHAN_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.DEL_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.FAV_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.UNFAV_RESTAU;

@AndroidEntryPoint
public class ActivityWithFrag extends AppCompatActivity {

    private ActivityWithFragBinding binding;
    private RestaurantViewModel restaurantViewModel;
    private final Context context = this;
    private User user;
    private UserListAdapter adapter;
    private List<Restaurant> restaurants;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWithFragBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        adapter = new UserListAdapter(this, BSD_ADA);

        NotifRestau.startWorker(this);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(binding.navView, navController);

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        NavController navDrawer = navHostFragment.getNavController();
        NavigationView navView = findViewById(R.id.navBar);
        NavigationUI.setupWithNavController(navView, navDrawer);

        restaurantViewModel.getListRestaurant().observe(this, restaurants -> this.restaurants = restaurants);

        restaurantViewModel.getUser().observe(this, userFirebase -> {
            user = userFirebase;

            View headerView = navView.getHeaderView(0);
            ImageView imgUser = headerView.findViewById(R.id.imgUser);
            TextView userName = headerView.findViewById(R.id.userName);
            TextView userEmail = headerView.findViewById(R.id.userEmail);

            Glide.with(this).load(user.getPhotoUrl())
                    .apply(new RequestOptions()
                            .circleCrop()
                            .format(DecodeFormat.PREFER_ARGB_8888)
                            .override(Target.SIZE_ORIGINAL))
                    .into(imgUser);
            userName.setText(user.getName());
            userEmail.setText(user.getEmail());
        });

    }

    public void openDrawer() {
        binding.drawerLayout.openDrawer(Gravity.LEFT);
    }

    public void openBottomSheetDialog(Restaurant restaurant, String fragment, GoogleMap googleMap) {
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

                } else if(newState == BottomSheetBehavior.STATE_COLLAPSED){

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
        collapsedBottom(restaurant, googleMap);
        expandedBottom(restaurant, googleMap);

        binding.bottomSheet.setVisibility(View.VISIBLE);
    }

    private void collapsedBottom(Restaurant restaurant, GoogleMap googleMap) {
        binding.picRestau.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), (int) (view.getHeight() + 40F), 40F);
            }
        });
        binding.picRestau.setClipToOutline(true);

        binding.nameRestau.setText(restaurant.getName());
        binding.adress.setText(restaurant.getVicinity());

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

    private void expandedBottom(Restaurant restaurant, GoogleMap googleMap) {
        binding.textNameRestau.setText(restaurant.getName());
        binding.textAdressRestau.setText(restaurant.getVicinity());
        Glide.with(context).load(restaurant.getPicUrl()).centerCrop().into(binding.restauImg);
        binding.chipsDistanceExpanded.setText("" + restaurant.getDistance() + " m");
        binding.phoneImage.setOnClickListener(v -> dialPhone(restaurant.getPhoneNumber()));

        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        Log.d("chipsTest", "day");
        HashMap<String, String> mapHourRestau = restaurant.setChips(day);

        String openCLose = mapHourRestau.get("isOpen");

        if (openCLose.equals("true")) {
            int remainTime = Integer.parseInt(mapHourRestau.get("remainTime"));
            String remainTimeStr = mapHourRestau.get("remainTimeStr");
            Log.d("remainTime", "" + mapHourRestau.get("remainTime"));
            if (remainTime <= 30) {
                binding.chipsHourExpanded.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_orange_dark)));
                binding.chipsHourExpanded.setText("ferme dans " + remainTimeStr);
            } else {
                binding.chipsHourExpanded.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_dark)));
                binding.chipsHourExpanded.setText("Open");
            }
        } else if (openCLose.equals("false")) {
            int remainTime = Integer.parseInt(mapHourRestau.get("remainTime"));
            String remainTimeStr = mapHourRestau.get("remainTimeStr");
            Log.d("remainTime", "" + mapHourRestau.get("remainTime"));
            if (remainTime <= 30) {
                binding.chipsHourExpanded.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_blue_bright)));

                binding.chipsHourExpanded.setText("ouvre dans " + remainTimeStr);
            } else {
                binding.chipsHourExpanded.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
                binding.chipsHourExpanded.setText("Close");
            }
        } else {
            binding.chipsHourExpanded.setChipBackgroundColor(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
            binding.chipsHourExpanded.setText("Close");
        }

        if (user.isRestauChoosen() && user.getThisDayRestau() != null && user.getThisDayRestau().getPlaceId().equals(restaurant.getPlaceId())) {
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
            if (user.getFavRestau() != null && containsRestaurant(user.getFavRestau(), restaurant)) {
                restaurantViewModel.favRestau(user, restaurant, UNFAV_RESTAU);
                binding.likeImage.setImageResource(R.drawable.ic_outline_star_rate_24);
            } else {
                restaurantViewModel.favRestau(user, restaurant, FAV_RESTAU);
                binding.likeImage.setImageResource(R.drawable.ic_baseline_star_rate_24);
            }
        });

        binding.fabEatButton.setOnClickListener(v -> {
            if (user.isRestauChoosen() && user.getThisDayRestau() != null && user.getThisDayRestau().getPlaceId().equals(restaurant.getPlaceId())) {
                restaurantViewModel.eatRestaurant(user, restaurant, DEL_RESTAU);
                user.setRestauChoosen(false);
                binding.fabEatButton.setImageResource(R.drawable.ic_outline_check_circle_24);
                if (googleMap != null) {
                    googleMap.clear();
                    EventBus.getDefault().post(new RefreshMarkers(restaurants));
                }

            } else if (user.isRestauChoosen() && user.getThisDayRestau() != null && !user.getThisDayRestau().getPlaceId().equals(restaurant.getPlaceId())) {
                restaurantViewModel.eatRestaurant(user, restaurant, CHAN_RESTAU);
                user.setRestauChoosen(true);
                user.setThisDayRestau(restaurant);
                binding.fabEatButton.setImageResource(R.drawable.ic_baseline_check_circle_24);
                if (googleMap != null) {
                    googleMap.clear();
                    EventBus.getDefault().post(new RefreshMarkers(restaurants));
                }

            } else {
                restaurantViewModel.eatRestaurant(user, restaurant, ADD_RESTAU);
                user.setRestauChoosen(true);
                user.setThisDayRestau(restaurant);
                binding.fabEatButton.setImageResource(R.drawable.ic_baseline_check_circle_24);
                if (googleMap != null) {
                    googleMap.clear();
                    EventBus.getDefault().post(new RefreshMarkers(restaurants));
                }
            }
        });


        if (restaurant.getWebsite() == null) binding.websiteImage.setEnabled(false);
        binding.websiteImage.setOnClickListener(v -> {
            Uri webpage = Uri.parse(restaurant.getWebsite());
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        binding.rvListMatesRestau.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if (restaurant.getListUser() != null) {
            for (User user1 : restaurant.getListUser()) {
                if (user1.getEmail().equals(user.getEmail())) {
                    restaurant.getListUser().remove(user1);
                }
            }
            binding.rvListMatesRestau.setAdapter(adapter);
            adapter.setUserList(restaurant.getListUser());
        }

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