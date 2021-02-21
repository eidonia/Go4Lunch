package com.openclassrooms.go4lunch.repository;

import android.app.usage.NetworkStats;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.event.ActivityFragEvent;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.models.details.DetailsRestaurant;
import com.openclassrooms.go4lunch.models.details.ResultDetails;
import com.openclassrooms.go4lunch.models.nearby.NearbyRestaurantResponse;
import com.openclassrooms.go4lunch.models.nearby.Result;
import com.openclassrooms.go4lunch.network.GoogleMapService;
import com.openclassrooms.go4lunch.ui.MainActivity;
import com.openclassrooms.go4lunch.ui.restaurant.ActivityWithFrag;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.openclassrooms.go4lunch.utils.Constante.ADD_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.API_KEY;
import static com.openclassrooms.go4lunch.utils.Constante.CHAN_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.DEL_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.FAV_RESTAU;
import static com.openclassrooms.go4lunch.utils.Constante.NAME_PIC;
import static com.openclassrooms.go4lunch.utils.Constante.STORAGE_REF;
import static com.openclassrooms.go4lunch.utils.Constante.UNFAV_RESTAU;

public class MapRepository {

    private final GoogleMapService googleMapService;
    private final DatabaseReference refRest;
    private final DatabaseReference refUser;
    private final StorageReference storage;
    private final ArrayList<Restaurant> restauList = new ArrayList<>();
    private final ArrayList<User> userList = new ArrayList<>();
    private int count = 0;
    private int countUser;
    private int listSize = 0;
    private final int radius = 300;

    @Inject
    public MapRepository(GoogleMapService googleMapService, @Named("restaurants") DatabaseReference refRest, @Named("users") DatabaseReference refUser, StorageReference storage) {
        this.googleMapService = googleMapService;
        this.refRest = refRest;
        this.refUser = refUser;
        this.storage = storage;
    }

    public void getNearbyRestaurant(LatLng loc) {
        Log.d("callApi", "Appel");
            String location = "" + loc.latitude + "," + loc.longitude;
            googleMapService.getNearbyRestaurant(location, radius, "restaurant", API_KEY)
                    .subscribeOn(Schedulers.io())
                    .map(nearbyRestaurantResponse -> {
                        ArrayList<Result> list = nearbyRestaurantResponse.getResults();
                        return list;
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new DisposableObserver<ArrayList<Result>>() {
                        @Override
                        public void onNext(@NonNull ArrayList<Result> results) {
                            Log.d("onNext", "on Next = here " + results.size());
                            count = 0;
                            listSize = results.size();
                            for (Result result : results) {
                                getDetailsRestaurant(result.getPlaceId(), loc);
                            }
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

    public LiveData<List<Restaurant>> searchRestaurant(String s, LatLng loc, RectangularBounds bounds, PlacesClient placesClient) {
        LiveData<List<Restaurant>> resultLiveData = new MutableLiveData<>();
        Log.d("QueryTest", "Repo " + s);
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setOrigin(loc)
                .setCountries("FR")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(s)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            Log.d("result", "Here");
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                if (prediction.getDistanceMeters() < 300) {
                    Log.i("QueryTest", prediction.getPrimaryText(null).toString() + "  " + prediction.getSecondaryText(null).toString());
                }

            }
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                Log.e("search", "Place not found: " + apiException.getStatusCode());
            }
        });

        return resultLiveData;
    }

    public void getDetailsRestaurant(String placeId, LatLng loc) {
        googleMapService.getDetailsRestaurant(placeId, "name,geometry,rating,international_phone_number,vicinity,opening_hours,photo,website", API_KEY)
                .subscribeOn(Schedulers.io())
                .map(detailsRestaurant -> {
                    ResultDetails result = detailsRestaurant.getResult();
                    return result;
                })
                .subscribe(new DisposableObserver<ResultDetails>() {
                    @Override
                    public void onNext(@NonNull ResultDetails result) {
                        Log.d("resultDetails", " 1 " + result.getName());
                        String picUrl = result.getPhotos() == null ? "" :
                                "https://maps.googleapis.com/maps/api/place/photo?maxwidth=400&photoreference=" + result.getPhotos().get(0).getPhotoReference() + "&key=" + API_KEY;
                        Gson gson =  new Gson();
                        if (result.getOpeningHours() != null){
                            try {
                                addRestaurant(result, placeId, picUrl, loc);
                            }catch (Exception e) {
                                Log.d("resultDetails", "exception " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("resultDetails", " onComplete " + restauList.size());

                    }
                });
    }

    public MutableLiveData<List<Restaurant>> getRestFromFirebase() {
        restauList.clear();
        MutableLiveData<List<Restaurant>> resultLiveData = new MutableLiveData<>();
        Query query = refRest;
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    restauList.add(snap.getValue(Restaurant.class));
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
        resultLiveData.postValue(restauList);

        return resultLiveData;
    }

    public MutableLiveData<User> getUserFirebase(String uid) {
        MutableLiveData<User> user = new MutableLiveData<>();
        Query query = refUser.child(uid);

        query.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                user.postValue(snapshot.getValue(User.class));
                //setMarkers()
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
        return user;
    }

    public LiveData<List<User>> getUsers(String uid) {
        LiveData<List<User>> users = new MutableLiveData<>();
        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                List<User> listUser = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    listSize++;
                }
                Log.d("datasnapshot", "" + listSize);
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.d("datasnapshot", "preIf  " + dataSnapshot.getKey());
                    incrementeCount();
                    if (!dataSnapshot.getKey().equals(uid)){
                        Log.d("datasnapshot", "preIf  " + dataSnapshot.getKey() + " " + uid);
                        listUser.add(dataSnapshot.getValue(User.class));

                    }
                }
                createListUser(listUser, (MutableLiveData<List<User>>) users);
                listUser.clear();
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });

        return users;
    }


    private void createListUser(List<User> user, MutableLiveData<List<User>> userLiveData) {
            Log.d("datasnapshot", "Passela " + user.size() + " " + user.get(0).getName());
            userLiveData.setValue(user);
    }

    private Integer distanceRest(Float lat, Float lon, LatLng loc) {
        Location baseLoc = new Location("location A");
        baseLoc.setLatitude(loc.latitude);
        baseLoc.setLongitude(loc.longitude);

        Location restLoc = new Location("location B");
        restLoc.setLatitude(lat);
        restLoc.setLongitude(lon);

        return (int) baseLoc.distanceTo(restLoc);
    }

    private Integer rating(Float rating) {
        double rateDec;
        double decimal;
        int rate;
        if (rating == null) {
            rate = 0;
        } else {
            rateDec = rating * 3;
            rateDec = rateDec / 5;
            decimal = rateDec - (int) rateDec;
            if (decimal < 0.5) {
                rate = (int) Math.floor(rateDec);
            } else {
                rate = (int) Math.ceil(rateDec);
            }
        }
        return rate;
    }

    private synchronized void incrementeCount(){
        count++;
    }

    private boolean containsRestaurant(MutableLiveData<List<Restaurant>> listRestau, String placeId){
        boolean contains = false;
        for (Restaurant restaurant : listRestau.getValue()) {
            if (restaurant.getPlaceId().equals(placeId)) {
                contains = true;
                break;
            }
        }
        return  contains;
    }

    private void addRestaurant(ResultDetails result, String placeId, String picUrl, LatLng loc) {
        refRest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                Log.d("user", "Here");
                //refUsers.push().setValue(new User(displayName, email, photoUrl.toString()));
                count = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Log.d("data", " data: " + data.getValue().toString());
                    if (data.getKey().equals(placeId)) {
                        count++;
                    }
                }
                if (count == 0) {
                    refRest.child(placeId).push().setValue(new Restaurant(result.getGeometry().getLocation().getLat(),
                            result.getGeometry().getLocation().getLng(), result.getName(),
                            placeId, rating(result.getRating()),
                            result.getVicinity(),
                            distanceRest(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(), loc),
                            result.getOpeningHours(),
                            result.getInternationalPhoneNumber(),
                            picUrl,
                            result.getWebsite(),
                            R.drawable.baseline_place_unbook_24,
                            null));
                    HashMap<String, Object> addNewRest = new HashMap<>();
                    addNewRest.put(placeId, new Restaurant(result.getGeometry().getLocation().getLat(),
                            result.getGeometry().getLocation().getLng(), result.getName(),
                            placeId, rating(result.getRating()),
                            result.getVicinity(),
                            distanceRest(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(), loc),
                            result.getOpeningHours(),
                            result.getInternationalPhoneNumber(),
                            picUrl,
                            result.getWebsite(),
                            R.drawable.baseline_place_unbook_24,
                            null));
                    refRest.updateChildren(addNewRest);
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    public void eatRestaurant(User user, Restaurant restaurant, String constRestau) {
        HashMap<String, Object> userMap = new HashMap<>();
        HashMap<String, Object> restauMap = new HashMap<>();
        switch (constRestau) {
            case DEL_RESTAU:
                userMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(), null, false));
                refUser.updateChildren(userMap);
                restaurant.getListUser().removeIf(user1 -> user1.getEmail().equals(user.getEmail()));
                restauMap.put(restaurant.getPlaceId(), new Restaurant(restaurant.getLatitude(), restaurant.getLongitude(), restaurant.getName(), restaurant.getPlaceId(), restaurant.getRating(), restaurant.getVicinity(), restaurant.getDistance(), restaurant.getOpeningHours(), restaurant.getPhoneNumber(), restaurant.getPicUrl(), restaurant.getWebsite(), restaurant.getMarker(), restaurant.getListUser()));
                refRest.updateChildren(restauMap);
                break;
            case CHAN_RESTAU:
                delUserRestau(user, user.getThisDayRestau());
                userMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(), restaurant, true));
                refUser.updateChildren(userMap);
                restaurant.getListUser().add(user);
                restauMap.put(restaurant.getPlaceId(), new Restaurant(restaurant.getLatitude(), restaurant.getLongitude(), restaurant.getName(), restaurant.getPlaceId(), restaurant.getRating(), restaurant.getVicinity(), restaurant.getDistance(), restaurant.getOpeningHours(), restaurant.getPhoneNumber(), restaurant.getPicUrl(), restaurant.getWebsite(), restaurant.getMarker(), restaurant.getListUser()));
                refRest.updateChildren(restauMap);
                break;
            case ADD_RESTAU:
                userMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(), restaurant, true));
                refUser.updateChildren(userMap);
                restaurant.getListUser().add(user);
                restauMap.put(restaurant.getPlaceId(), new Restaurant(restaurant.getLatitude(), restaurant.getLongitude(), restaurant.getName(), restaurant.getPlaceId(), restaurant.getRating(), restaurant.getVicinity(), restaurant.getDistance(), restaurant.getOpeningHours(), restaurant.getPhoneNumber(), restaurant.getPicUrl(), restaurant.getWebsite(), restaurant.getMarker(), restaurant.getListUser()));
                refRest.updateChildren(restauMap);
                break;
        }
    }

    public void favRestau(User user, Restaurant restaurant, String constRestau) {
        HashMap<String, Object> userMap = new HashMap<>();
        if (constRestau.equals(UNFAV_RESTAU)) {
            user.getFavRestau().removeIf(restaurant1 -> restaurant1.getPlaceId().equals(restaurant.getPlaceId()));
            userMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(),user.getFavRestau(),user.getThisDayRestau(), user.isRestauChoosen()));
            refUser.updateChildren(userMap);
        } else if (constRestau.equals(FAV_RESTAU)) {
            user.getFavRestau().add(restaurant);
            userMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), user.getPhotoUrl(), user.getFavRestau(),user.getThisDayRestau(), user.isRestauChoosen()));
            refUser.updateChildren(userMap);
        }
    }

    public void addUser(String displayName, String email, String uid, Uri photoUrl) {
        refUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                Log.d("user", "Here");
                countUser = 0;
                for (DataSnapshot data : snapshot.getChildren()) {
                    Log.d("data", " data: " + data.getValue().toString());
                    if (data.child("name").getValue().toString().equals(displayName)) {
                        Log.d("dataName", " dataName: " + data.child("name").toString() + " displayName: " + displayName);
                        countUser++;
                    }
                }
                if (countUser == 0) {
                    if (photoUrl != null) {
                        User user  = new User(displayName, email, photoUrl.toString(), null, null, false);
                        refUser.child(uid).push().setValue(user);
                        HashMap<String, Object> addNewUser = new HashMap<>();
                        addNewUser.put(uid, user);
                        refUser.updateChildren(addNewUser);
                    } else {

                        StorageReference picRef = storage.child(STORAGE_REF + "/" + NAME_PIC);
                        picRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            User user  = new User(displayName, email, uri.toString(), null, null, false);
                            refUser.child(uid).push().setValue(user);
                            HashMap<String, Object> addNewUser = new HashMap<>();
                            addNewUser.put(uid, user);
                            refUser.updateChildren(addNewUser);
                        });

                    }
                }

                EventBus.getDefault().post(new ActivityFragEvent());
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    public void changePic(String uid, User user, Uri photoUri) {
        StorageReference picRef = storage.child(uid + "/" + photoUri.getLastPathSegment());
        UploadTask uploadTask = picRef.putFile(photoUri);

        uploadTask.addOnFailureListener(e -> Log.d("uplaodTask", "" + e.fillInStackTrace()))
                .addOnSuccessListener(taskSnapshot -> Log.d("uplaodTask", "Success"));

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw  task.getException();
            }

            return picRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            Uri downloadUrl = task.getResult();

            HashMap<String, Object> userMap = new HashMap<>();
            userMap.put(FirebaseAuth.getInstance().getCurrentUser().getUid(), new User(user.getName(), user.getEmail(), downloadUrl.toString(), user.getFavRestau(), user.getThisDayRestau(), user.isRestauChoosen()));
            refUser.updateChildren(userMap);
        });
    }

    public void changeName(String uid, User user) {
        HashMap<String, Object> usermap = new HashMap<>();
        usermap.put(uid, user);
        refUser.updateChildren(usermap);
    }

    private void delUserRestau(User user, Restaurant restaurant) {
        HashMap<String, Object> restauMap = new HashMap<>();
        restaurant.getListUser().removeIf(user1 -> user1.getEmail().equals(user.getEmail()));
        restauMap.put(restaurant.getPlaceId(), new Restaurant(restaurant.getLatitude(), restaurant.getLongitude(), restaurant.getName(), restaurant.getPlaceId(), restaurant.getRating(), restaurant.getVicinity(), restaurant.getDistance(), restaurant.getOpeningHours(), restaurant.getPhoneNumber(), restaurant.getPicUrl(), restaurant.getWebsite(), restaurant.getMarker(), restaurant.getListUser()));
        refRest.updateChildren(restauMap);
    }
}
