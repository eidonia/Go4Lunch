package com.openclassrooms.go4lunch.ui.restaurant.list;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.gms.maps.model.LatLng;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentListBinding;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.util.HashMap;

import javax.inject.Inject;

import static android.content.Context.LOCATION_SERVICE;

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

        restaurantViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
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
                if (s.length() >= 3) {
                    HashMap<String, String> infoRestau = new HashMap<>();
                    infoRestau.put("query", s.toString());
                    infoRestau.put("lat",  String.valueOf(location.getLatitude()));
                    infoRestau.put("lon", String.valueOf(location.getLongitude()));
                    restaurantViewModel.setRestauQueryList(infoRestau);
                }else if (s.length() < 3) {

                    restaurantViewModel.setLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }
}