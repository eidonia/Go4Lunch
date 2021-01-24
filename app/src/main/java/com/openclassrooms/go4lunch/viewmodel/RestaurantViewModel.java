package com.openclassrooms.go4lunch.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.gson.Gson;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.details.ResultDetails;
import com.openclassrooms.go4lunch.models.nearby.Result;
import com.openclassrooms.go4lunch.repository.MapRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.openclassrooms.go4lunch.utils.Constante.API_KEY;

public class RestaurantViewModel extends ViewModel {

    private MapRepository mapRepository;
    RectangularBounds bounds;
    private PlacesClient placesClient;
    private MutableLiveData<LatLng> latLng = new MutableLiveData<>();
    private LiveData<List<Restaurant>> restaurantList = Transformations.switchMap(latLng, input -> mapRepository.getNearbyRestaurant(input));
    private MutableLiveData<HashMap<String, String>> queryRestau= new MutableLiveData<>();
    private LiveData<List<Restaurant>> restauQueryList = Transformations.switchMap(queryRestau, input -> mapRepository.searchRestaurant(input.get("query"), new LatLng(Double.parseDouble(input.get("lat")), Double.parseDouble(input.get("lon"))), bounds, placesClient));

    @SuppressLint("MissingPermission")
    @ViewModelInject
    public RestaurantViewModel(MapRepository mapRepository, Application app) {
        this.mapRepository = mapRepository;
        Places.initialize(app, "AIzaSyA0GSIW6MM_L0MwQlRtlhX_m6g-UoRu5I0");
        placesClient = Places.createClient(app);
    }

    public void setLatLng(LatLng latLng) {
        this.latLng.postValue(latLng);
    }

    public void setRestauQueryList(HashMap<String, String> query) {
        this.queryRestau.postValue(query);
        bounds = RectangularBounds.newInstance(
                getCoordinate(Double.parseDouble(query.get("lat")), Double.parseDouble(query.get("lon")), -1000, -1000),
                getCoordinate(Double.parseDouble(query.get("lat")), Double.parseDouble(query.get("lon")), 1000, 1000)
        );
    }

    public LiveData<List<Restaurant>> getRestaurants() {
        return restaurantList;
    }
    public LiveData<List<Restaurant>> getRestaurantQuery(){ return restauQueryList; }


    public static LatLng getCoordinate(double lat0, double lng0, long dy, long dx) {
        double lat = lat0 + (180 / Math.PI) * (dy / 6378137);
        double lng = lng0 + (180 / Math.PI) * (dx / 6378137) / Math.cos(lat0);
        return new LatLng(lat, lng);
    }
}
