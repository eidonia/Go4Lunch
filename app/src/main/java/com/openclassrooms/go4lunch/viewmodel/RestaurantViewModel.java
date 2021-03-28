package com.openclassrooms.go4lunch.viewmodel;

import android.annotation.SuppressLint;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;
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

    private final MapRepository mapRepository;

    @SuppressLint("MissingPermission")
    @Inject
    public RestaurantViewModel(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public void setLatLng(LatLng latLng) {
        mapRepository.getNearbyRestaurant(latLng);
    }

    public void getUsersFbRoom() {
        mapRepository.getUsers();
    }

    public MutableLiveData<List<User>> getUsersRoom() {
        return mapRepository.getUsersRoom();
    }

    public MutableLiveData<List<Restaurant>> getRestauQueryList(String s) {
        return mapRepository.searchRestaurant(s);
    }

    public MutableLiveData<List<Restaurant>> getListRestaurant() {
        return mapRepository.getRestFromFirebase();
    }

    public MutableLiveData<User> getUser() {
        return mapRepository.getUserFirebase();
    }

    public MutableLiveData<User> getUserFirebaseMessage(String email) {
        return mapRepository.getUserFirebaseMessage(email);
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

    public void changePic(Uri photoUri) {
        mapRepository.changePic(photoUri);
    }

    public void changeName(String changeName) {
        mapRepository.changeName(changeName);
    }
}
