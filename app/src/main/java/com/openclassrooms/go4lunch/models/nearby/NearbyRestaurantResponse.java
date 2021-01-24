package com.openclassrooms.go4lunch.models.nearby;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NearbyRestaurantResponse {

    private List<Object> htmlAttributions;
    private ArrayList<Result> results;
    private String status;
    private Map<String, Object> additionalProperties;

    public NearbyRestaurantResponse(List<Object> htmlAttributions, ArrayList<Result> results, String status, Map<String, Object> additionalProperties) {
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

    public ArrayList<Result> getResults() {
        return results;
    }

    public void setResults(ArrayList<Result> results) {
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

