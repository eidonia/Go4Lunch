package com.openclassrooms.go4lunch.ui.restaurant.list;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.openclassrooms.go4lunch.databinding.FragmentListBinding;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.util.Collections;

import javax.inject.Inject;

public class ListFragment extends Fragment {

    @Inject
    public Location location;
    private FragmentListBinding binding;
    private RestaurantViewModel restaurantViewModel;
    private ListRestAdapter restAdapter;

    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();

        restAdapter = new ListRestAdapter(getContext());
        binding.listRestau.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        binding.listRestau.setAdapter(restAdapter);

        restaurantViewModel = new ViewModelProvider(this.getActivity()).get(RestaurantViewModel.class);

        restaurantViewModel.getListRestaurant().observe(getViewLifecycleOwner(), restaurants -> {
            Collections.sort(restaurants, (restaurant, t1) -> restaurant.getDistance().compareTo(t1.getDistance()));
            restAdapter.updateRestauList(restaurants);
        });


        binding.toolbar.setNavigationOnClickListener(v -> {
            ((ActivityWithFrag)getActivity()).openDrawer();
        });

        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 6) {
                    restaurantViewModel.getRestauQueryList(s.toString()).observe(getViewLifecycleOwner(), restaurants -> {
                        Log.d("Query", "observer " + restaurants.size());
                        //Collections.sort(restaurants, (restaurant, t1) -> restaurant.getDistance().compareTo(t1.getDistance()));
                        restAdapter.updateRestauList(restaurants);
                    });

                } else if (s.length() == 0) {

                    restaurantViewModel.getListRestaurant().observe(getViewLifecycleOwner(), restaurants -> {
                        Collections.sort(restaurants, (restaurant, t1) -> restaurant.getDistance().compareTo(t1.getDistance()));
                        restAdapter.updateRestauList(restaurants);
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }
}