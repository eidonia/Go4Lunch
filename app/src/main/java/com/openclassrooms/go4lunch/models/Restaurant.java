package com.openclassrooms.go4lunch.models;

import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.PlusCode;
import com.google.maps.android.data.Geometry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Restaurant {

    private String businessStatus;
    //private Geometry geometry;
    private String icon;
    private String name;
    private OpeningHours openingHours;
    private String placeId;
    private Integer priceLevel;
    private Double rating;
    private String reference;
    private String scope;
    private List<String> types = null;
    private Integer userRatingsTotal;
    private String vicinity;
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();


    public String getBusinessStatus() {
        return businessStatus;
    }


    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    /*public Geometry getGeometry() {
        return geometry;
    }*/

    /*public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }*/

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OpeningHours getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(OpeningHours openingHours) {
        this.openingHours = openingHours;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }
    /*public PlusCode getPlusCode() {
        return plusCode;
    }
    public void setPlusCode(PlusCode plusCode) {
        this.plusCode = plusCode;
    }
*/
    public Integer getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(Integer priceLevel) {
        this.priceLevel = priceLevel;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public Integer getUserRatingsTotal() {
        return userRatingsTotal;
    }

    public void setUserRatingsTotal(Integer userRatingsTotal) {
        this.userRatingsTotal = userRatingsTotal;
    }

    public String getVicinity() {
        return vicinity;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }
}
