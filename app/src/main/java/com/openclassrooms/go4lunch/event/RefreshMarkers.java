package com.openclassrooms.go4lunch.event;

import com.openclassrooms.go4lunch.models.Restaurant;

import java.util.List;

public class RefreshMarkers {
    private final List<Restaurant> restaurants;

    public RefreshMarkers(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

    public List<Restaurant> getRestaurants() {
        return restaurants;
    }
}
