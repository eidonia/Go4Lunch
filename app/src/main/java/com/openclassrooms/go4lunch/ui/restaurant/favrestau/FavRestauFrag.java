package com.openclassrooms.go4lunch.ui.restaurant.favrestau;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openclassrooms.go4lunch.databinding.FragmentFavRestauBinding;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.ui.restaurant.list.ListRestAdapter;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FavRestauFrag extends Fragment {

    private FragmentFavRestauBinding binding;
    private ListRestAdapter restAdapter;
    private RestaurantViewModel restaurantViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = FragmentFavRestauBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> {
            ((ActivityWithFrag) getActivity()).openDrawer();
        });

        restAdapter = new ListRestAdapter(getActivity());
        binding.listRestau.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.listRestau.setAdapter(restAdapter);

        restaurantViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            restAdapter.updateRestauList(user.getFavRestau());
        });

        return view;
    }
}
