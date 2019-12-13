package com.example.travelo.Models;

import java.util.List;

public class Route {
    String name;
    LatLang origin;
    List<LatLang> waypoints;
    LatLang destination;
    List<LatLang> bounds;
    String mode = "bicycling";
    String type;
    Long distance;
    String distanceText;
    Long time;
    String timeText;


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

    public Long getDistance() {
        return distance;
    }

    public void setDistance(Long distance) {
        this.distance = distance;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDistanceText() {
        return distanceText;
    }

    public void setDistanceText(String distanceText) {
        this.distanceText = distanceText;
    }

    public String getTimeText() {
        return timeText;
    }

    public void setTimeText(String timeText) {
        this.timeText = timeText;
    }
}
