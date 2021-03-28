package com.openclassrooms.go4lunch.ui.restaurant.todayrestau;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentTodayRestauBinding;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.restaurant.workmates.UserListAdapter;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import static com.openclassrooms.go4lunch.utils.Constante.BSD_ADA;
import static com.openclassrooms.go4lunch.utils.Constante.FAV_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.UNFAV_RESTAU;

@AndroidEntryPoint
public class TodayRestauFragment extends Fragment {

    private FragmentTodayRestauBinding binding;
    private RestaurantViewModel restaurantViewModel;
    private User user;
    private UserListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentTodayRestauBinding.inflate(inflater);
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        adapter = new UserListAdapter(getContext(), BSD_ADA);

        restaurantViewModel.getUser().observe(getViewLifecycleOwner(), userFirebase -> {
            user = userFirebase;
            if (user.getThisDayRestau() != null) {
                Restaurant restaurant = user.getThisDayRestau();
                binding.viewRestau.setVisibility(View.VISIBLE);
                binding.noRestauView.setVisibility(View.GONE);
                createView(restaurant);
            } else {
                binding.viewRestau.setVisibility(View.GONE);
                binding.noRestauView.setVisibility(View.VISIBLE);
            }
        });

        return binding.getRoot();
    }

    private void createView(Restaurant restaurant) {
        binding.textNameRestau.setText(restaurant.getName());
        binding.textAdressRestau.setText(restaurant.getVicinity());
        Glide.with(getContext()).load(restaurant.getPicUrl()).centerCrop().into(binding.restauImg);
        binding.chipsDistanceExpanded.setText(getContext().getString(R.string.distanceRestau, restaurant.getDistance()));
        binding.phoneImage.setOnClickListener(v -> dialPhone(restaurant.getPhoneNumber()));

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

        if (restaurant.getWebsite() == null) binding.websiteImage.setEnabled(false);
        binding.websiteImage.setOnClickListener(v -> {
            Uri webpage = Uri.parse(restaurant.getWebsite());
            Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
            if (intent.resolveActivity(getContext().getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        binding.rvListMatesRestau.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.rvListMatesRestau.setAdapter(adapter);
        adapter.setUserList(restaurant.getListUser());
    }

    private void dialPhone(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    public boolean containsRestaurant(final List<Restaurant> rest, Restaurant restaurant) {
        boolean contains = false;
        for (Restaurant item : rest) {
            if (item.getPlaceId().equals(restaurant.getPlaceId())) {
                contains = true;
            }
        }
        return contains;
    }
}