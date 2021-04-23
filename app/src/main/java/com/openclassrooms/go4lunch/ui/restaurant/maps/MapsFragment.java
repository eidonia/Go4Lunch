package com.openclassrooms.go4lunch.ui.restaurant.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentMapsBinding;
import com.openclassrooms.go4lunch.event.RefreshMarkers;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

import static android.content.Context.LOCATION_SERVICE;

@AndroidEntryPoint
public class MapsFragment extends Fragment implements OnMapReadyCallback {

    @Inject
    public Location location;
    private FragmentMapsBinding binding;
    private RestaurantViewModel restaurantViewModel;
    private final HashMap<Marker, Restaurant> hashMap = new HashMap<>();
    private LocationManager locationManager;
    private GoogleMap gMap;
    private List<Restaurant> restaurants;


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        EventBus.getDefault().register(this);
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        restaurantViewModel = new ViewModelProvider(this.getActivity()).get(RestaurantViewModel.class);
        binding.mapView.onCreate(savedInstanceState);


        restaurantViewModel.getListRestaurant().observe(getViewLifecycleOwner(), restaurants -> {
            try {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            this.restaurants = restaurants;
            binding.mapView.getMapAsync(this);
        });

        binding.btnMenu.setOnClickListener(v ->
                ((ActivityWithFrag) getActivity()).openDrawer()
        );

        return view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.style_map));
        googleMap.clear();

        View locationButton = ((View) binding.mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_START);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0f);
        googleMap.animateCamera(update);

        setMarkers(restaurants);

        googleMap.setOnMarkerClickListener(marker -> {
            Restaurant restaurant = hashMap.get(marker);
            try {
                ((ActivityWithFrag) getActivity()).openBottomSheetDialog(restaurant, "maps", googleMap);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return true;
        });



        binding.completeSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() >= 6) {
                    restaurantViewModel.getRestauQueryList(s.toString()).observe(getViewLifecycleOwner(), restaurants -> {
                        googleMap.clear();
                        setMarkers(restaurants);
                    });

                } else if (s.length() == 0) {

                    restaurantViewModel.getListRestaurant().observe(getViewLifecycleOwner(), restaurants -> {
                        googleMap.clear();
                        setMarkers(restaurants);
                    });
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        binding.mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setMarkers(List<Restaurant> listNet) {
        for (Restaurant restaurant : listNet) {
            int marker = R.drawable.baseline_place_unbook_24;
            if (restaurant.getListUser() != null && restaurant.getListUser().size() > 0) {
                marker = R.drawable.baseline_place_booked_24;
            }
            hashMap.put(gMap.addMarker(new MarkerOptions()
                            .position(new LatLng(restaurant.getLatitude(), restaurant.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(marker)))
                    , restaurant);
        }
    }

    @Subscribe
    public void onRefreshMarkers(RefreshMarkers event) {
        restaurantViewModel.getListRestaurant().observe(getViewLifecycleOwner(), restaurants -> {
            setMarkers(restaurants);
        });
    }
}