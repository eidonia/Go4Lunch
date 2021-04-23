package com.openclassrooms.go4lunch.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.List;

@Entity
public class User {

    @PrimaryKey
    @NonNull
    private String email = "";
    private String name;
    private String photoUrl;
    @Ignore
    private List<Restaurant> favRestau = new ArrayList<>();
    @Ignore
    private Restaurant thisDayRestau;
    private String thisDayRestauStr;
    private boolean isRestauChoosen;
    private String ejabberdName;
    @Ignore
    private String ejabberdPsswd;
    private boolean status;

    @Ignore
    public User() {
    }

    public User(String name, String email, String photoUrl, String thisDayRestauStr, boolean isRestauChoosen, String ejabberdName) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.thisDayRestauStr = thisDayRestauStr;
        this.isRestauChoosen = isRestauChoosen;
        this.ejabberdName = ejabberdName;
    }

    @Ignore
    public User(String name, String email, String photoUrl, Restaurant thisDayRestau, boolean isRestauChoosen, String ejabberdName, String ejabberdPsswd) {
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.thisDayRestau = thisDayRestau;
        this.isRestauChoosen = isRestauChoosen;
        this.ejabberdName = ejabberdName;
        this.ejabberdPsswd = ejabberdPsswd;
    }

    @Ignore
    public User(String name, String email, String photoUrl, String ejabberdName) {
        this.email = email;
        this.name = name;
        this.photoUrl = photoUrl;
        this.ejabberdName = ejabberdName;
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

    public String getEjabberdName() {
        return ejabberdName;
    }

    public void setEjabberdName(String ejabberdName) {
        this.ejabberdName = ejabberdName;
    }

    public String getEjabberdPsswd() {
        return ejabberdPsswd;
    }

    public void setEjabberdPsswd(String ejabberdPsswd) {
        this.ejabberdPsswd = ejabberdPsswd;
    }

    public String getThisDayRestauStr() {
        return thisDayRestauStr;
    }

    public void setThisDayRestauStr(String thisDayRestauStr) {
        this.thisDayRestauStr = thisDayRestauStr;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
