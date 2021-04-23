package com.openclassrooms.go4lunch.network;

import com.openclassrooms.go4lunch.models.details.DetailsRestaurant;
import com.openclassrooms.go4lunch.models.nearby.NearbyRestaurantResponse;
import com.openclassrooms.go4lunch.models.nearby.Result;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoogleMapService {

    @GET("nearbysearch/json")
    Observable<NearbyRestaurantResponse> getNearbyRestaurant(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

    @GET("details/json")
    Observable<DetailsRestaurant> getDetailsRestaurant(@Query("place_id") String id, @Query("fields") String fields, @Query("key") String key);
}
