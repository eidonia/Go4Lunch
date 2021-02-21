package com.openclassrooms.go4lunch.models;

import android.util.Log;

import com.openclassrooms.go4lunch.models.details.OpeningHours;
import com.openclassrooms.go4lunch.models.details.Period;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.openclassrooms.go4lunch.utils.Constante.DEC_FOR;

public class Restaurant {

    private Float latitude;
    private Float longitude;
    private String name;
    private String placeId;
    private Integer rating;
    private String vicinity;
    private Integer distance;
    private OpeningHours openingHours;
    private String phoneNumber;
    private String picUrl;
    private String website;
    private int marker;
    private List<User> listUser = new ArrayList<>();

    public Restaurant() {
    }

    public Restaurant(Float latitude, Float longitude, String name, String placeId, Integer rating, String vicinity, Integer distance, OpeningHours openingHours, String phoneNumber, String picUrl, String website, int marker, List<User> listUser) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.placeId = placeId;
        this.rating = rating;
        this.vicinity = vicinity;
        this.distance = distance;
        this.openingHours = openingHours;
        this.phoneNumber = phoneNumber;
        this.picUrl = picUrl;
        this.website = website;
        this.marker = marker;
        this.listUser = listUser;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public int getMarker() {
        return marker;
    }

    public void setMarker(int marker) {
        this.marker = marker;
    }

    public void getActualStatus() {
        Calendar calendar = Calendar.getInstance();
        Log.d("dayOfWeek", "" + calendar.get(Calendar.DAY_OF_WEEK));
        String actualHour = new DecimalFormat(DEC_FOR).format((calendar.get(Calendar.HOUR_OF_DAY)));
        String actualMinute = new DecimalFormat(DEC_FOR).format((calendar.get(Calendar.MINUTE)));
        String concatHourMinute = actualHour + actualMinute;
        Log.d("dayOfWeek", "" + concatHourMinute + "  " + Integer.valueOf(concatHourMinute));
        int countPeriod = 0;
        List<String> periodOpen = new ArrayList<>();
        List<String> periodClose = new ArrayList<>();
        if (openingHours.getOpenNow()) { //open so looking for close
            for (Period period : openingHours.getPeriods()) {
                Log.d("dayOfWeek", "" + (period.getClose().getDay() + 1) + "  " + calendar.get(Calendar.DAY_OF_WEEK));
                if (period.getClose().getDay() == calendar.get(Calendar.DAY_OF_WEEK)) {
                    countPeriod++;
                    periodClose.add(period.getClose().getTime());
                }

                if (period.getOpen().getDay() == calendar.get(Calendar.DAY_OF_WEEK)) {
                    countPeriod++;
                    periodClose.add(period.getClose().getTime());
                }

            }
            if (periodClose.size() == 1) {
                if (Integer.parseInt(concatHourMinute) > 1000 && Integer.parseInt(periodClose.get(0)) < 1000) {
                    if (Math.abs(Integer.parseInt(concatHourMinute) - (Integer.parseInt(periodClose.get(0)) + 2400)) < 300) {
                        Log.d("CloseSoon", "Le restaurant ferme dans moins d'une heure");
                    } else {
                        Log.d("CloseSoon", "Ouvert");
                    }
                } else {
                    if (Math.abs(Integer.parseInt(concatHourMinute) - (Integer.parseInt(periodClose.get(0)))) < 300) {
                        Log.d("CloseSoon", "Le restaurant ferme dans moins d'une heure");
                    } else {
                        Log.d("CloseSoon", "Ouvert");
                    }
                }
            } else if (periodClose.size() == 2) {

            }
        }
    }

    public List<User> getListUser() {
        return listUser;
    }

    public void setListUser(List<User> listUser) {
        this.listUser = listUser;
    }
}
