package com.openclassrooms.go4lunch.ui.restaurant.maps;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
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
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.FragmentMapsBinding;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.hilt.android.AndroidEntryPoint;

import static android.content.Context.LOCATION_SERVICE;
import static com.openclassrooms.go4lunch.utils.Constante.MAPS_FRAG;

@AndroidEntryPoint
public class MapsFragment extends Fragment implements OnMapReadyCallback {


    @Inject
    @Named("users")
    public DatabaseReference refUsers;
    @Inject
    public Location location;
    private FragmentMapsBinding binding;
    private FragmentActivity fragActivity;
    private RestaurantViewModel restaurantViewModel;
    private final HashMap<Marker, Restaurant> hashMap = new HashMap<>();
    private User user;
    private FirebaseUser firebaseUser;
    private LocationManager locationManager;
    private GoogleMap gMap;


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("MissingPermission")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMapsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        binding.mapView.onCreate(savedInstanceState);
        restaurantViewModel = new ViewModelProvider(this.getActivity()).get(RestaurantViewModel.class);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        binding.mapView.getMapAsync(this);

        binding.btnMenu.setOnClickListener(v ->
            ((ActivityWithFrag)getActivity()).openDrawer()
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
        Log.d("LatLong", "" + location.getLatitude() + " " + location.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 17.0f);
        googleMap.animateCamera(update);

        restaurantViewModel.getUser(firebaseUser.getUid()).observe(getViewLifecycleOwner(), userFirebase -> user = userFirebase);
        restaurantViewModel.getListRestaurant().observe(getViewLifecycleOwner(), restaurants -> {
            Log.d("restaurant", "maps " + restaurants.size());
                setMarkers(restaurants);
        });

        googleMap.setOnMarkerClickListener(marker -> {
            Restaurant restaurant = hashMap.get(marker);
            ((ActivityWithFrag)getActivity()).openBottomSheetDialog(restaurant, "maps");
            Log.d("marker", " " + restaurant.getName() + " " + restaurant.getPhoneNumber() + " " + restaurant.getPlaceId());
            return true;
        });



        binding.completeSearch.addTextChangedListener(new TextWatcher() {
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

                    restaurantViewModel.getRestaurants();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

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

    @SuppressLint("MissingPermission")
    @Override
    public void onResume() {
        binding.mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
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


    public void setMarkers(List<Restaurant> listNet) {
        for (Restaurant restaurant : listNet) {
            int marker = R.drawable.baseline_place_unbook_24;
            if (restaurant.getListUser().size() > 0) {
                marker = R.drawable.baseline_place_booked_24;
            }
            hashMap.put(gMap.addMarker(new MarkerOptions()
                            .position(new LatLng(restaurant.getLatitude(), restaurant.getLongitude()))
                            .icon(BitmapDescriptorFactory.fromResource(marker)))
                    , restaurant);
        }
    }
}