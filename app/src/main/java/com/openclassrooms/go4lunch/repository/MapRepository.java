package com.openclassrooms.go4lunch.repository;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.details.DetailsRestaurant;
import com.openclassrooms.go4lunch.models.details.ResultDetails;
import com.openclassrooms.go4lunch.models.nearby.NearbyRestaurantResponse;
import com.openclassrooms.go4lunch.models.nearby.Result;
import com.openclassrooms.go4lunch.network.GoogleMapService;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static com.openclassrooms.go4lunch.utils.Constante.API_KEY;

public class MapRepository {

    private GoogleMapService googleMapService;
    private DatabaseReference refRest;
    private ArrayList<Restaurant> restauList = new ArrayList<>();
    private int count = 0;
    private int listSize = 0;
    private int radius = 300;

    @Inject
    public MapRepository(GoogleMapService googleMapService, @Named("restaurants") DatabaseReference refRest) {
        this.googleMapService = googleMapService;
        this.refRest = refRest;
    }

    public LiveData<List<Restaurant>> getNearbyRestaurant(LatLng loc) {
        Log.d("onNext", "Coucou");
        LiveData<List<Restaurant>> resultLiveData = new MutableLiveData<>();
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
                                getDetailsRestaurant(result.getPlaceId(), (MutableLiveData<List<Restaurant>>) resultLiveData, loc);
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
        return  resultLiveData;
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
                    getDetailsRestaurant(prediction.getPlaceId(), (MutableLiveData<List<Restaurant>>) resultLiveData, loc);
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

    public void getDetailsRestaurant(String placeId, MutableLiveData<List<Restaurant>> resultLiveData, LatLng loc) {
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
                                restauList.add(new Restaurant(
                                        result.getGeometry().getLocation().getLat(),
                                        result.getGeometry().getLocation().getLng(), result.getName(),
                                        placeId, rating(result.getRating()),
                                        result.getVicinity(),
                                        distanceRest(result.getGeometry().getLocation().getLat(), result.getGeometry().getLocation().getLng(), loc),
                                        result.getOpeningHours(),
                                        result.getInternationalPhoneNumber(),
                                        picUrl,
                                        result.getWebsite(),
                                        R.drawable.baseline_place_unbook_24
                                ));
                            }catch (Exception e) {
                                Log.d("resultDetails", "exception " + e.getMessage());
                            }
                        }
                        Log.d("resultWebsite", "" + result.getWebsite());

                        //Log.d("resultDetails", " 2 " + picUrl);
                        incrementeCount();
                        Log.d("resultDetails", " 2count " + count + " " + listSize);
                        if(count == listSize) {
                            Log.d("resultDetails", " 2compar " + restauList.size());
                            resultLiveData.postValue(restauList);
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
}
