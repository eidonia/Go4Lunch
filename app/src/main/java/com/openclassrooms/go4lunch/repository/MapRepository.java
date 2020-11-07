package com.openclassrooms.go4lunch.repository;

import com.openclassrooms.go4lunch.models.NearbyRestaurantResponse;
import com.openclassrooms.go4lunch.network.GoogleMapService;

import javax.inject.Inject;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static com.openclassrooms.go4lunch.utils.Constante.API_KEY;

public class MapRepository {

    private GoogleMapService googleMapService;

    @Inject
    public MapRepository(GoogleMapService googleMapService) {
        this.googleMapService = googleMapService;
    }

    public Observable<NearbyRestaurantResponse> getNearbyRestaurant(String location, int radius) {
        return googleMapService.getNearbyRestaurant(location, radius, "restaurant", API_KEY);
    }


}
