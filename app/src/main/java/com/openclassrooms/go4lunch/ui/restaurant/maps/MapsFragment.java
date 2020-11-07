package com.openclassrooms.go4lunch.ui.restaurant.maps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
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
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            location = locationManager.getLastKnownLocation(provider);
        }

        binding.mapView.getMapAsync(this);

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
        rlp.setMargins(0, 0, 0, 250);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0f);
        googleMap.animateCamera(update);
        restaurantViewModel.getNearbyRestaurants("" + location.getLatitude() + "," + location.getLongitude(), 300);
        restaurantViewModel.getRestaurants().observe(this, restaurants -> {
            //create marker pour chaque restaurant
        });

        RectangularBounds bounds = RectangularBounds.newInstance(
                getCoordinate(location.getLatitude(), location.getLongitude(), -300, -300),
                getCoordinate(location.getLatitude(), location.getLongitude(), 300, 300)
        );

        AutocompleteSupportFragment autoCompleteFragment = (AutocompleteSupportFragment) getChildFragmentManager().findFragmentById(R.id.completeSearch);
        autoCompleteFragment.setLocationBias(bounds);
        autoCompleteFragment.setCountry("FR");
        autoCompleteFragment.setTypeFilter(TypeFilter.ESTABLISHMENT);
        autoCompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS, Place.Field.ADDRESS_COMPONENTS, Place.Field.BUSINESS_STATUS, Place.Field.OPENING_HOURS, Place.Field.PHONE_NUMBER, Place.Field.PHOTO_METADATAS, Place.Field.RATING, Place.Field.WEBSITE_URI));
        autoCompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //Log.i("Kointe", "Place: " + place.getName() + ", " + ", " + place.getPhoneNumber() + ", " + place.getOpeningHours().getPeriods().get(2) + ", " + place.getPhotoMetadatas());
                Log.i("Kointe", "Place: " + autoCompleteFragment);
                Log.i("Kointe", "Place: " + googleMap);
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 17.0f);
                googleMap.animateCamera(update);
            }

            @Override
            public void onError(@NonNull Status status) {
                Log.i("Kointe", "An error occurred: " + status);
            }
        });

        googleMap.addCircle(new CircleOptions()
        .center(new LatLng(location.getLatitude(), location.getLongitude()))
                .radius(300)
                .strokeColor(Color.BLUE)
                .strokeWidth(2));
        googleMap.setOnPoiClickListener(this);

    }

    public static com.google.android.gms.maps.model.LatLng getCoordinate(double lat0, double lng0, long dy, long dx) {
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