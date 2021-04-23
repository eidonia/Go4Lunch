package com.openclassrooms.go4lunch.event;

public class ReceiveEvent {

    private String name;

    public ReceiveEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
