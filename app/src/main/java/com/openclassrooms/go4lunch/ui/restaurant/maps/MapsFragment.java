package com.openclassrooms.go4lunch.ui.restaurant.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.FindCurrentPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;

import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentMapsBinding;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

import static android.content.Context.LOCATION_SERVICE;

@AndroidEntryPoint
public class MapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnPoiClickListener {

    private FragmentMapsBinding binding;
    private FragmentActivity fragActivity;
    private PlacesClient placesClient;
    private Location location;
    private RestaurantViewModel restaurantViewModel;

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        binding.mapView.onCreate(savedInstanceState);

        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);
        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Places.initialize(getContext(), "AIzaSyA0GSIW6MM_L0MwQlRtlhX_m6g-UoRu5I0");
        placesClient = Places.createClient(getContext());

        LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(provider);

        binding.mapView.getMapAsync(this);

        binding.btnMenu.setOnClickListener(v ->
            ((ActivityWithFrag)getActivity()).openDrawer()
        );

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_map));


        View locationButton = ((View) binding.mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_START);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0f);
        googleMap.animateCamera(update);
        restaurantViewModel.getNearbyRestaurants("" + location.getLatitude() + "," + location.getLongitude(), 300);
        restaurantViewModel.getRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            Log.d("here", "" + restaurants.size());
        });

        RectangularBounds bounds = RectangularBounds.newInstance(
                getCoordinate(location.getLatitude(), location.getLongitude(), -300, -300),
                getCoordinate(location.getLatitude(), location.getLongitude(), 300, 300)
        );

        binding.completeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                restaurantViewModel.SearchRestaurant(s, bounds, placesClient, location.getLatitude(), location.getLongitude());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        googleMap.addCircle(new CircleOptions()
        .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(300)
                .strokeColor(Color.BLUE)
                .strokeWidth(2));
        googleMap.setOnPoiClickListener(this);

    }

    public static LatLng getCoordinate(double lat0, double lng0, long dy, long dx) {
        double lat = lat0 + (180 / Math.PI) * (dy / 6378137);
        double lng = lng0 + (180 / Math.PI) * (dx / 6378137) / Math.cos(lat0);
        return new com.google.android.gms.maps.model.LatLng(lat, lng);
    }

    @Override
    public void onPoiClick(PointOfInterest pointOfInterest) {
        Log.i("Kointe", "" + pointOfInterest.name + " " + pointOfInterest.placeId + " " + pointOfInterest.latLng);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding.mapView.onDestroy();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        fragActivity = (FragmentActivity) context;
        super.onAttach(context);
    }

}