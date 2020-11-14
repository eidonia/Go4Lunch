package com.openclassrooms.go4lunch.viewmodel;

import android.util.Log;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.models.NearbyRestaurantResponse;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.repository.MapRepository;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.http.Query;

public class RestaurantViewModel extends ViewModel {

    private MapRepository mapRepository;
    private MutableLiveData<ArrayList<Restaurant>> restaurantList = new MutableLiveData<>();

    @ViewModelInject
    public RestaurantViewModel(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public MutableLiveData<ArrayList<Restaurant>> getRestaurants() { return restaurantList;}

    public void getNearbyRestaurants(String location, int radius) {
        mapRepository.getNearbyRestaurant(location, radius)
                .subscribeOn(Schedulers.io())
                .map(nearbyRestaurantResponse -> {
                    ArrayList<Restaurant> list = nearbyRestaurantResponse.getResults();
                    return list;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableObserver<ArrayList<Restaurant>>() {
                    @Override
                    public void onNext(@NonNull ArrayList<Restaurant> restaurants) {
                        Log.d("onNext", "on Next = here " + restaurants.size());
                        restaurantList.setValue(restaurants);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d("onNext", "on Next = " + e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        Log.d("onNext", "on Complete = here");
                    }
                });
    }

    public void SearchRestaurant(CharSequence s, RectangularBounds bounds, PlacesClient placesClient, double latitude, double longitude) {
        String search = s.toString();
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                //.setLocationBias(bounds)
                //.setLocationRestriction(bounds)
                .setOrigin(new LatLng(latitude, longitude))
                .setCountries("FR")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(search)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                Log.i("search", prediction.getPlaceId());
                Log.i("search", prediction.getPrimaryText(null).toString());

            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("search", "Place not found: " + apiException.getStatusCode());
            }
        });

    }

}
