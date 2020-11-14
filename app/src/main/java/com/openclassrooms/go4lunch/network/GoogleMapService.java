package com.openclassrooms.go4lunch.network;

import com.openclassrooms.go4lunch.models.NearbyRestaurantResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GoogleMapService {

    @GET("nearbysearch/json")
    Observable<NearbyRestaurantResponse> getNearbyRestaurant(@Query("location") String location, @Query("radius") int radius, @Query("type") String type, @Query("key") String key);

    @GET("nearbysearch/json")
    Observable<NearbyRestaurantResponse> getSearchedRestaurant(@Query("location") String location, @Query("radius") int radius,@Query("input") String input, @Query("inputype") String inputype, @Query("type") String type, @Query("key") String key);
}
