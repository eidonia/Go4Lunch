package com.openclassrooms.go4lunch.models.details;

import java.util.List;

public class DetailsRestaurant {
    private List<Object> htmlAttributions = null;
    private ResultDetails result;
    private String status;

    public List<Object> getHtmlAttributions() {
        return htmlAttributions;
    }

    public void setHtmlAttributions(List<Object> htmlAttributions) {
        this.htmlAttributions = htmlAttributions;
    }

    public ResultDetails getResult() {
        return result;
    }

    public void setResult(ResultDetails result) {
        this.result = result;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
