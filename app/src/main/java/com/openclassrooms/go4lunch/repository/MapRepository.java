package com.openclassrooms.go4lunch.repository;

import android.app.Application;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.event.ActivityFragEvent;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.models.details.ResultDetails;
import com.openclassrooms.go4lunch.models.nearby.Result;
import com.openclassrooms.go4lunch.network.GoogleMapService;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
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
    private final StorageReference storage;
    private final FirebaseFirestore db;
    private final ArrayList<Restaurant> restauList = new ArrayList<>();
    private final ArrayList<User> userList = new ArrayList<>();
    private int count = 0;
    private int countUser;
    private int listSize = 0;
    private final int radius = 300;
    private final Location loc;
    private final PlacesClient placesClient;
    private final ArrayList<Restaurant> resultPredic = new ArrayList<>();
    boolean isExist = false;

    @Inject
    public MapRepository(GoogleMapService googleMapService, StorageReference storage, FirebaseFirestore db, Location location, Application app) {
        this.googleMapService = googleMapService;
        this.storage = storage;
        this.loc = location;
        this.db = db;
        Places.initialize(app, API_KEY);
        placesClient = Places.createClient(app);
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

    public static LatLng getCoordinate(double lat0, double lng0, long dy, long dx) {
        double lat = lat0 + (180 / Math.PI) * (dy / 6378137);
        double lng = lng0 + (180 / Math.PI) * (dx / 6378137) / Math.cos(lat0);
        return new LatLng(lat, lng);
    }

    public MutableLiveData<List<Restaurant>> searchRestaurant(String s) {
        Log.d("QueryTest", "Repo " + s);
        MutableLiveData<List<Restaurant>> resultLiveData = new MutableLiveData<>();
        List<String> placeIdPredic = new ArrayList<>();
        RectangularBounds bounds = RectangularBounds.newInstance(
                getCoordinate(loc.getLatitude(), loc.getLongitude(), -1000, -1000),
                getCoordinate(loc.getLatitude(), loc.getLatitude(), 1000, 1000)
        );
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(bounds)
                .setOrigin(new LatLng(loc.getLatitude(), loc.getLongitude()))
                .setCountries("FR")
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery(s)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener((response) -> {
            Log.d("result", "Here");
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                if (prediction.getDistanceMeters() < 300) {
                    Log.d("QueryTest", "prediction " + prediction.getPrimaryText(null).toString() + "  " + prediction.getPlaceId());
                    placeIdPredic.add(prediction.getPlaceId());
                }
            }

            getRestauFromPrediction(placeIdPredic, resultLiveData);
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

    public void getRestauFromPrediction(List<String> placeId, MutableLiveData<List<Restaurant>> restauList) {
        List<Restaurant> restauFromPredic = new ArrayList<>();
        db.collection("restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (String place : placeId) {
                        for (QueryDocumentSnapshot queryDocuments : queryDocumentSnapshots) {
                            Restaurant restaurant = queryDocuments.toObject(Restaurant.class);
                            if (place.equals(restaurant.getPlaceId())) {
                                restauFromPredic.add(restaurant);
                            }
                        }
                    }
                    Log.d("QueryTest", "restauFromPredic " + restauFromPredic.size());
                    restauList.postValue(restauFromPredic);
                });
    }

    public MutableLiveData<List<Restaurant>> getRestFromFirebase() {
        restauList.clear();
        MutableLiveData<List<Restaurant>> resultLiveData = new MutableLiveData<>();
        db.collection("restaurants")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Restaurant restaurant = documentSnapshot.toObject(Restaurant.class);
                        if (distanceRest(restaurant.getLatitude(), restaurant.getLongitude(), new LatLng(loc.getLatitude(), loc.getLongitude())) <= 300) {
                            restaurant.setDistance(distanceRest(restaurant.getLatitude(), restaurant.getLongitude(), new LatLng(loc.getLatitude(), loc.getLongitude())));
                            restauList.add(restaurant);
                        }
                    }
                    resultLiveData.postValue(restauList);
                });

        return resultLiveData;
    }

    public MutableLiveData<Restaurant> getChoosenRestau(String placedId) {
        restauList.clear();
        MutableLiveData<Restaurant> restChoosen = new MutableLiveData<>();
        db.collection("restaurants").document(placedId)
                .get()
                .addOnSuccessListener(documentSnapshot -> restChoosen.postValue(documentSnapshot.toObject(Restaurant.class)));

        return restChoosen;
    }

    public MutableLiveData<User> getUserFirebase() {
        MutableLiveData<User> user = new MutableLiveData<>();
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> user.postValue(documentSnapshot.toObject(User.class)));
        return user;
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

    private synchronized void incrementeCount() {
        count++;
    }

    public MutableLiveData<List<User>> getUsers() {
        MutableLiveData<List<User>> users = new MutableLiveData<>();

        db.collection("users")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> listUser = new ArrayList<>();
                    for (QueryDocumentSnapshot dataSnapshot : queryDocumentSnapshots) {
                        if (!dataSnapshot.getId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            listUser.add(dataSnapshot.toObject(User.class));
                        }
                    }
                    if (listUser.size() > 0) {
                        createListUser(listUser, users);
                        listUser.clear();
                    }
                });

        return users;
    }

    private void addRestaurant(ResultDetails result, String placeId, String picUrl, LatLng loc) {
        countUser = 0;
        db.collection("restaurants").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    Restaurant restaurant = document.toObject(Restaurant.class);
                    Log.d("testId", "restauID : " + restaurant.getPlaceId() + "  -  " + "placeId : " + placeId);
                    if (restaurant.getPlaceId().equals(placeId)) {
                        updateRestau(result, placeId);
                        countUser++;
                        break;
                    }
                }
                if (countUser == 0) {
                    newRestau(result, placeId, picUrl);
                }
            }
        });
    }

    private void newRestau(ResultDetails result, String placeId, String picUrl) {
        Log.d("testId", "restauID : added");
        db.collection("restaurants").document(placeId)
                .set(new Restaurant(result.getGeometry().getLocation().getLat(),
                        result.getGeometry().getLocation().getLng(), result.getName(),
                        placeId, rating(result.getRating()),
                        result.getVicinity(),
                        result.getOpeningHours(),
                        result.getInternationalPhoneNumber(),
                        picUrl,
                        result.getWebsite(),
                        R.drawable.baseline_place_unbook_24
                ))
                .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));
    }

    private void updateRestau(ResultDetails result, String placeId) {
        Log.d("testId", "restauID : added");
        db.collection("restaurants").document(placeId)
                .update("openingHours", result.getOpeningHours())
                .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));
    }

    public void eatRestaurant(User user, Restaurant restaurant, String constRestau) {
        switch (constRestau) {
            case DEL_RESTAU:

                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("thisDayRestau", null)
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));

                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("restauChoosen", false)
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));

                restaurant.getListUser().removeIf(user1 -> user1.getEmail().equals(user.getEmail()));
                db.collection("restaurants").document(restaurant.getPlaceId())
                        .update("listUser", restaurant.getListUser())
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));
                break;

            case CHAN_RESTAU:
                delUserRestau(user, user.getThisDayRestau());

                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("thisDayRestau", restaurant)
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));

                restaurant.getListUser().add(user);
                db.collection("restaurants").document(restaurant.getPlaceId())
                        .update("listUser", restaurant.getListUser())
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));
                break;

            case ADD_RESTAU:
                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("thisDayRestau", restaurant)
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));

                db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update("restauChoosen", true)
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));

                restaurant.getListUser().add(user);
                db.collection("restaurants").document(restaurant.getPlaceId())
                        .update("listUser", restaurant.getListUser())
                        .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                        .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));
                break;
        }
    }

    public void favRestau(User user, Restaurant restaurant, String constRestau) {
        if (constRestau.equals(UNFAV_RESTAU)) {

            user.getFavRestau().removeIf(restaurant1 -> restaurant1.getPlaceId().equals(restaurant.getPlaceId()));
            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update("favRestau", user.getFavRestau())
                    .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                    .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));

        } else if (constRestau.equals(FAV_RESTAU)) {

            user.getFavRestau().add(restaurant);
            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update("favRestau", user.getFavRestau())
                    .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                    .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));
        }
    }

    public void addUser(String displayName, String email, Uri photoUrl) {
        countUser = 0;
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    if (user.getEmail().equals(email)) {
                        countUser++;
                        break;
                    }
                }
                if (countUser == 0) {
                    Log.d("addUser", "Coucou");
                    if (photoUrl != null) {
                        User user = new User(displayName, email, photoUrl.toString(), null, false);
                        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .set(user)
                                .addOnFailureListener(e -> Log.d("addUser", "erreur: " + e))
                                .addOnSuccessListener(documentReference -> Log.d("addUser", "Success"));

                    } else {
                        StorageReference picRef = storage.child(STORAGE_REF + "/" + NAME_PIC);
                        picRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            User user = new User(displayName, email, uri.toString(), null, false);
                            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .set(user)
                                    .addOnFailureListener(e -> Log.d("addUser", "erreur: " + e))
                                    .addOnSuccessListener(documentReference -> Log.d("addUser", "Success"));
                        });
                    }
                }
                EventBus.getDefault().post(new ActivityFragEvent());
            }
        });
    }

    public void changePic(User user, Uri photoUri) {
        StorageReference picRef = storage.child(FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" + photoUri.getLastPathSegment());
        UploadTask uploadTask = picRef.putFile(photoUri);

        uploadTask.addOnFailureListener(e -> Log.d("uplaodTask", "" + e.fillInStackTrace()))
                .addOnSuccessListener(taskSnapshot -> Log.d("uplaodTask", "Success"));

        Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }

            return picRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            Uri downloadUrl = task.getResult();

            db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .update("photoUrl", downloadUrl.toString())
                    .addOnFailureListener(e -> Log.e("changePic", "erreur : " + e.getMessage()))
                    .addOnSuccessListener(aVoid -> Log.d("changePic", "Success"));
        });
    }

    public void changeName(String changeName) {
        db.collection("users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .update("name", changeName)
                .addOnFailureListener(e -> Log.e("changeName", "erreur : " + e.getMessage()))
                .addOnSuccessListener(aVoid -> Log.d("changeName", "Success"));
    }

    private void delUserRestau(User user, Restaurant restaurant) {
        restaurant.getListUser().removeIf(user1 -> user1.getEmail().equals(user.getEmail()));
        db.collection("restaurants").document(restaurant.getPlaceId())
                .update("listUser", restaurant.getListUser())
                .addOnFailureListener(e -> Log.e("delUser", "erreur : " + e.getMessage()))
                .addOnSuccessListener(aVoid -> Log.d("delUser", "Success"));
    }
}
