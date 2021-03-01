package com.openclassrooms.go4lunch.models;

import android.util.Log;

import com.openclassrooms.go4lunch.models.details.OpeningHours;
import com.openclassrooms.go4lunch.models.details.Period;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    public Restaurant(Float latitude, Float longitude, String name, String placeId, Integer rating, String vicinity, OpeningHours openingHours, String phoneNumber, String picUrl, String website, int marker) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.placeId = placeId;
        this.rating = rating;
        this.vicinity = vicinity;
        this.openingHours = openingHours;
        this.phoneNumber = phoneNumber;
        this.picUrl = picUrl;
        this.website = website;
        this.marker = marker;
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

    public List<User> getListUser() {
        return listUser;
    }

    public void setListUser(List<User> listUser) {
        this.listUser = listUser;
    }

    public HashMap<String, String> setChips(int day) {
        day -= 1;
        HashMap<String, String> hourMap = new HashMap<>();
        List<Period> listPeriods = new ArrayList<>();
        Log.d("chipsTest", "setCHips " + day);
        if (openingHours.getOpenNow()) {
            for (Period period : openingHours.getPeriods()) {
                if (period.getOpen().getDay() == day) {
                    Log.d("chipsTest", "getOpenNow true " + day);
                    listPeriods.add(period);
                }
            }
        } else {
            Log.d("chipsTest", "getOpenNow false " + openingHours.getPeriods().size());
            for (Period period : openingHours.getPeriods()) {
                Log.d("chipsTest", "getOpenNow false" + day);
                if (period.getClose().getDay() == day) {
                    listPeriods.add(period);
                }
            }
        }

        if (listPeriods.isEmpty()) {
            Log.d("chipsTest", "close allday" + day);
            hourMap.put("isOpen", "closeAllday");
        } else if (listPeriods.size() == 1) {
            Log.d("chipsTest", "open 1 time" + day);
            return getOnePeriod(listPeriods, openingHours.getOpenNow());
        } else {
            Log.d("chipsTest", "open 2 times" + day);
            return getTwoPeriods(listPeriods, openingHours.getOpenNow());
        }

        return hourMap;
    }

    private HashMap<String, String> getOnePeriod(List<Period> periods, Boolean openNow) {
        Log.d("chipsTest", "Coucou 1 time");
        HashMap<String, String> hourMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdfInt = new SimpleDateFormat("HHmm");
        String thisTime = sdf.format(cal.getTime());
        String thisTimeInt = sdfInt.format(cal.getTime());

        Period firstPeriod = periods.get(0);

        LocalTime start = LocalTime.parse(thisTime);
        String closePeriodStr = firstPeriod.getClose().getTime();
        String closePeriodStrTemp = closePeriodStr.substring(0, 2) + ":" + closePeriodStr.substring(2);
        LocalTime closePeriodLocal = LocalTime.parse(closePeriodStrTemp);
        String openPeriodStr = firstPeriod.getOpen().getTime();
        String openPeriodStrTemp = openPeriodStr.substring(0, 2) + ":" + openPeriodStr.substring(2);
        LocalTime openPeriodLocal = LocalTime.parse(openPeriodStrTemp);

        int actualTimeInt = Integer.parseInt(thisTimeInt);
        int remainTime;
        int closePeriod = Integer.parseInt(firstPeriod.getClose().getTime());
        int openPeriod = Integer.parseInt(firstPeriod.getOpen().getTime());
        String remainTimeStr = "00";

        Log.d("remainTime", "thisTime : " + thisTime + " - closePeriod : " + closePeriod + " - openPeriod" + openPeriod);

        if (openNow) { //OPEN
            hourMap.put("isOpen", openNow.toString());
            hourMap.put("hour", closePeriodStrTemp);
            remainTime = actualTimeInt - closePeriod;
            remainTimeStr = calTime(start, closePeriodLocal);

        } else { //CLOSE
            hourMap.put("isOpen", openNow.toString());
            hourMap.put("hour", openPeriodStrTemp);
            remainTime = actualTimeInt - openPeriod;
            remainTimeStr = calTime(start, openPeriodLocal);
        }
        hourMap.put("remainTime", String.valueOf(Math.abs(remainTime)));
        hourMap.put("remainTimeStr", remainTimeStr);
        return hourMap;
    }

    private HashMap<String, String> getTwoPeriods(List<Period> periods, Boolean openNow) {
        Log.d("chipsTest", "Coucou 2 times");
        HashMap<String, String> hourMap = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        SimpleDateFormat sdfInt = new SimpleDateFormat("HHmm");
        String thisTime = sdf.format(cal.getTime());
        String thisTimeInt = sdfInt.format(cal.getTime());

        Period firstPeriod = periods.get(0);
        Period secondPeriod = periods.get(1);

        LocalTime start = LocalTime.parse(thisTime);
        String closeFirstPeriod = firstPeriod.getClose().getTime();
        String closeFirstPeriodTemp = closeFirstPeriod.substring(0, 2) + ":" + closeFirstPeriod.substring(2);
        LocalTime firstPeriodClose = LocalTime.parse(closeFirstPeriodTemp);
        String closeSecondPeriod = secondPeriod.getClose().getTime();
        String closeSecondPeriodTemp = closeSecondPeriod.substring(0, 2) + ":" + closeSecondPeriod.substring(2);
        LocalTime secondPeriodClose = LocalTime.parse(closeSecondPeriodTemp);
        String openFirstPeriod = firstPeriod.getOpen().getTime();
        String openFirstPeriodTemp = openFirstPeriod.substring(0, 2) + ":" + openFirstPeriod.substring(2);
        LocalTime firstPeriodOpen = LocalTime.parse(openFirstPeriodTemp);
        String openSecondPeriod = secondPeriod.getOpen().getTime();
        String openSecondPeriodTemp = openSecondPeriod.substring(0, 2) + ":" + openSecondPeriod.substring(2);
        LocalTime secondPeriodOpen = LocalTime.parse(openSecondPeriodTemp);

        int actualTimeInt = Integer.parseInt(thisTimeInt);
        int closeFirstPeriodInt = Integer.parseInt(firstPeriod.getClose().getTime());
        int closeSecondPeriodInt = Integer.parseInt(secondPeriod.getClose().getTime());
        int openFirstPeriodInt = Integer.parseInt(firstPeriod.getOpen().getTime());
        int openSecondPeriodInt = Integer.parseInt(secondPeriod.getOpen().getTime());
        int remainTime = 00;
        String remainTimeStr = "00";

        if (openNow) { //OPEN
            hourMap.put("isOpen", openNow.toString());
            if (actualTimeInt > closeFirstPeriodInt && actualTimeInt > openSecondPeriodInt && actualTimeInt < closeSecondPeriodInt) {
                hourMap.put("hour", closeSecondPeriodTemp);
                remainTime = actualTimeInt - closeSecondPeriodInt;
                remainTimeStr = calTime(start, secondPeriodClose);
            } else if (actualTimeInt > openFirstPeriodInt && actualTimeInt < closeFirstPeriodInt) {
                hourMap.put("hour", closeFirstPeriodTemp);
                remainTime = actualTimeInt - closeFirstPeriodInt;
                remainTimeStr = calTime(start, firstPeriodClose);
            }
        } else { //CLOSE
            hourMap.put("isOpen", openNow.toString());
            if (actualTimeInt > closeFirstPeriodInt && actualTimeInt < openSecondPeriodInt) {
                hourMap.put("hour", openSecondPeriodTemp);
                remainTime = actualTimeInt - openSecondPeriodInt;
                remainTimeStr = calTime(start, secondPeriodOpen);
            } else if (actualTimeInt < openFirstPeriodInt && actualTimeInt > closeSecondPeriodInt) {
                hourMap.put("hour", openFirstPeriodTemp);
                remainTime = actualTimeInt - openFirstPeriodInt;
                remainTimeStr = calTime(start, firstPeriodOpen);
            }
        }

        hourMap.put("remainTime", String.valueOf(Math.abs(remainTime)));
        hourMap.put("remainTimeStr", remainTimeStr);
        return hourMap;
    }

    private String calTime(LocalTime start, LocalTime end) {
        Duration timeBetween = Duration.between(start, end);
        Log.d("calTime", "" + timeBetween.toMillis());
        int hour = (int) TimeUnit.MILLISECONDS.toHours(timeBetween.toMillis());
        int minute = (int) (TimeUnit.MILLISECONDS.toMinutes(timeBetween.toMillis()) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeBetween.toMillis())));
        if (minute > 60) {
            minute -= 60;
            hour += 1;
        } else if (minute == 60) {
            minute = 0;
            hour += 1;
        }

        String hourTemp = String.valueOf(hour);
        String minuteTemp = String.valueOf(minute);

        String hourStr = new DecimalFormat(DEC_FOR).format(hour);
        String minuteStr = new DecimalFormat(DEC_FOR).format(minute);

        return "" + hourStr + ":" + minuteStr;
    }
}
