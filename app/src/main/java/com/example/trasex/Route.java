package com.example.trasex;

import java.util.List;

public class Route {
    com.example.trasex.LatLang origin;
    List<LatLang> waypoints;
    com.example.trasex.LatLang destination;
    List<com.example.trasex.LatLang> bounds;
    String mode = "bicycling";
    String type = "";


    public Route() {
    }

    public LatLang getOrigin() {
        return origin;
    }

    public void setOrigin(LatLang origin) {
        this.origin = origin;
    }

    public List<LatLang> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<LatLang> waypoints) {
        this.waypoints = waypoints;
    }

    public LatLang getDestination() {
        return destination;
    }

    public void setDestination(LatLang destination) {
        this.destination = destination;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<LatLang> getBounds() {
        return bounds;
    }

    public void setBounds(List<LatLang> bounds) {
        this.bounds = bounds;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
