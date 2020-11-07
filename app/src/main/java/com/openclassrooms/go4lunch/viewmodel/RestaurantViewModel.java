package com.openclassrooms.go4lunch.viewmodel;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.openclassrooms.go4lunch.models.NearbyRestaurantResponse;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.repository.MapRepository;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Result;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
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
                .subscribe(result -> restaurantList.setValue(result));
    }

}
