package com.openclassrooms.go4lunch.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.net.Uri;
import android.util.Log;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.repository.MapRepository;

import java.util.HashMap;
import java.util.List;

import static com.openclassrooms.go4lunch.utils.Constante.API_KEY;

public class RestaurantViewModel extends ViewModel {

    private MapRepository mapRepository;
    RectangularBounds bounds;
    private PlacesClient placesClient;
    private final MutableLiveData<String> userUid = new MutableLiveData<>();
    private final LiveData<List<User>> userList = Transformations.switchMap(userUid, input -> mapRepository.getUsers(input));
    private final MutableLiveData<HashMap<String, String>> queryRestau= new MutableLiveData<>();
    private MutableLiveData<List<Restaurant>> listRestFromFirebase = new MutableLiveData<>();
    private final LiveData<List<Restaurant>> restauQueryList = Transformations.switchMap(queryRestau, input -> mapRepository.searchRestaurant(input.get("query"), new LatLng(Double.parseDouble(input.get("lat")), Double.parseDouble(input.get("lon"))), bounds, placesClient));

    @SuppressLint("MissingPermission")
    @ViewModelInject
    public RestaurantViewModel(MapRepository mapRepository, Application app) {
        this.mapRepository = mapRepository;
        Places.initialize(app, API_KEY);
        placesClient = Places.createClient(app);
    }

    public void setLatLng(LatLng latLng) {
        mapRepository.getNearbyRestaurant(latLng);
    }

    public void setUid(String uid) {
        this.userUid.setValue(uid);
    }

    public void setRestauQueryList(HashMap<String, String> query) {
        this.queryRestau.postValue(query);
        bounds = RectangularBounds.newInstance(
                getCoordinate(Double.parseDouble(query.get("lat")), Double.parseDouble(query.get("lon")), -1000, -1000),
                getCoordinate(Double.parseDouble(query.get("lat")), Double.parseDouble(query.get("lon")), 1000, 1000)
        );
    }

    public void getRestaurants() {
        this.listRestFromFirebase = mapRepository.getRestFromFirebase();
    }

    public MutableLiveData<List<Restaurant>> getListRestaurant() {
        Log.d("doudou", "PasseIci " + listRestFromFirebase.getValue());
        return listRestFromFirebase;
    }
    public LiveData<List<Restaurant>> getRestaurantQuery(){ return restauQueryList; }

    public MutableLiveData<User> getUser(String uid) {
        return mapRepository.getUserFirebase(uid);
    }

    public LiveData<List<User>> getUserList() { return userList;}

    public void eatRestaurant(User user, Restaurant restaurant, String constRestau) {
        mapRepository.eatRestaurant(user, restaurant, constRestau);
    }

    public void favRestau(User user, Restaurant restaurant, String constRestau) {
        mapRepository.favRestau(user, restaurant, constRestau);
    }

    public void addUser(String displayName, String email, String uid, Uri photoUrl) {
        mapRepository.addUser(displayName, email, uid, photoUrl);
    }

    public void changePic(String uid, User user, Uri photoUri) {
        mapRepository.changePic(uid, user, photoUri);
    }

    public void changeName(String uid, User user) {
        mapRepository.changeName(uid, user);
    }

    public static LatLng getCoordinate(double lat0, double lng0, long dy, long dx) {
        double lat = lat0 + (180 / Math.PI) * (dy / 6378137);
        double lng = lng0 + (180 / Math.PI) * (dx / 6378137) / Math.cos(lat0);
        return new LatLng(lat, lng);
    }
}
