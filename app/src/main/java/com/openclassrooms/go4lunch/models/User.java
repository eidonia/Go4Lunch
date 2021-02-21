package com.openclassrooms.go4lunch.models;

import java.util.ArrayList;
import java.util.List;

public class User {

    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private List<Restaurant> favRestau = new ArrayList<>();
    private Restaurant thisDayRestau;
    private boolean isRestauChoosen;

    public User() {
    }

    public User(String name, String email, String photoUrl, Restaurant thisDayRestau, boolean isRestauChoosen) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.thisDayRestau = thisDayRestau;
        this.isRestauChoosen = isRestauChoosen;
    }

    public User(String name, String email, String photoUrl, List<Restaurant> favRestau, Restaurant thisDayRestau, boolean isRestauChoosen) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.favRestau = favRestau;
        this.thisDayRestau = thisDayRestau;
        this.isRestauChoosen = isRestauChoosen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Restaurant getThisDayRestau() {
        return thisDayRestau;
    }

    public void setThisDayRestau(Restaurant thisDayRestau) {
        this.thisDayRestau = thisDayRestau;
    }

    public boolean isRestauChoosen() {
        return isRestauChoosen;
    }

    public void setRestauChoosen(boolean restauChoosen) {
        isRestauChoosen = restauChoosen;
    }

    public List<Restaurant> getFavRestau() {
        return favRestau;
    }

    public void setFavRestau(List<Restaurant> favRestau) {
        this.favRestau = favRestau;
    }
}
