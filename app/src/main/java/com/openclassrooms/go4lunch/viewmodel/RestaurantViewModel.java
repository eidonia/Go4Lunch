package com.openclassrooms.go4lunch.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.repository.MapRepository;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RestaurantViewModel extends ViewModel {

    private MapRepository mapRepository;
    private final MutableLiveData<String> queryRestau = new MutableLiveData<>();
    private final LiveData<List<Restaurant>> restauQueryList = Transformations.switchMap(queryRestau, input -> mapRepository.searchRestaurant(input));

    @SuppressLint("MissingPermission")
    @Inject
    public RestaurantViewModel(MapRepository mapRepository, Application app) {
        this.mapRepository = mapRepository;

    }

    public void setLatLng(LatLng latLng) {
        mapRepository.getNearbyRestaurant(latLng);
    }

    public MutableLiveData<List<User>> getUserList() {
        return mapRepository.getUsers();
    }

    public MutableLiveData<List<Restaurant>> getRestauQueryList(String s) {
        return mapRepository.searchRestaurant(s);
    }

    public MutableLiveData<List<Restaurant>> getListRestaurant() {
        return mapRepository.getRestFromFirebase();
    }

    public MutableLiveData<Restaurant> getRestChoosen(String placeId) {
        return mapRepository.getChoosenRestau(placeId);
    }

    public MutableLiveData<User> getUser() {
        return mapRepository.getUserFirebase();
    }

    public void eatRestaurant(User user, Restaurant restaurant, String constRestau) {
        mapRepository.eatRestaurant(user, restaurant, constRestau);
    }

    public void favRestau(User user, Restaurant restaurant, String constRestau) {
        mapRepository.favRestau(user, restaurant, constRestau);
    }

    public void addUser(String displayName, String email, Uri photoUrl) {
        mapRepository.addUser(displayName, email, photoUrl);
    }

    public void changePic(User user, Uri photoUri) {
        mapRepository.changePic(user, photoUri);
    }

    public void changeName(String changeName) {
        mapRepository.changeName(changeName);
    }
}
