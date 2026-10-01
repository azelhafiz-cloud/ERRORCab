package com.errorcab.copilot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Smart day balance model showing proportional allocation across travel activities.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DayBalance {
    private int walkingPercent;
    private int sightseeingPercent;
    private int foodPercent;
    private int relaxationPercent;
    private int travelPercent;

    public DayBalance() {
        this.walkingPercent = 20;
        this.sightseeingPercent = 40;
        this.foodPercent = 15;
        this.relaxationPercent = 15;
        this.travelPercent = 10;
    }

    public DayBalance(int walkingPercent, int sightseeingPercent, int foodPercent,
                      int relaxationPercent, int travelPercent) {
        this.walkingPercent = walkingPercent;
        this.sightseeingPercent = sightseeingPercent;
        this.foodPercent = foodPercent;
        this.relaxationPercent = relaxationPercent;
        this.travelPercent = travelPercent;
    }

    public int getWalkingPercent() {
        return walkingPercent;
    }

    public void setWalkingPercent(int walkingPercent) {
        this.walkingPercent = walkingPercent;
    }

    public int getSightseeingPercent() {
        return sightseeingPercent;
    }

    public void setSightseeingPercent(int sightseeingPercent) {
        this.sightseeingPercent = sightseeingPercent;
    }

    public int getFoodPercent() {
        return foodPercent;
    }

    public void setFoodPercent(int foodPercent) {
        this.foodPercent = foodPercent;
    }

    public int getRelaxationPercent() {
        return relaxationPercent;
    }

    public void setRelaxationPercent(int relaxationPercent) {
        this.relaxationPercent = relaxationPercent;
    }

    public int getTravelPercent() {
        return travelPercent;
    }

    public void setTravelPercent(int travelPercent) {
        this.travelPercent = travelPercent;
    }
}
