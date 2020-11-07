package com.openclassrooms.go4lunch.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;

public class NearbyRestaurantResponse {

    private List<Object> htmlAttributions;
    private ArrayList<Restaurant> results;
    private String status;
    private Map<String, Object> additionalProperties;

    public NearbyRestaurantResponse(List<Object> htmlAttributions, ArrayList<Restaurant> results, String status, Map<String, Object> additionalProperties) {
        this.htmlAttributions = htmlAttributions;
        this.results = results;
        this.status = status;
        this.additionalProperties = additionalProperties;
    }

    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public ArrayList<Restaurant> getResults() {
        return results;
    }

    public void setResults(ArrayList<Restaurant> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}

