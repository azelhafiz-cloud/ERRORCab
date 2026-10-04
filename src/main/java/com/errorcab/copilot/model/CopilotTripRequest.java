package com.errorcab.copilot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object encapsulating user-specified travel preferences
 * collected by the ERRORCab AI Travel Copilot.
 */
public class CopilotTripRequest {
    private int passengerId;
    private String startingLocation;  // e.g. "Kochi", "Delhi", "Bengaluru", "Mumbai", or current GPS location
    private String tripPurpose;       // e.g. "Leisure & Tourism", "Business & Work", "Weekend Getaway", "Culinary Exploration", "Family Outing"
    private String destination;       // e.g. "Fort Kochi", "Kakkanad", "Edappally", "Munnar", "Alappuzha", "Vyttila"
    private String duration;          // e.g. "Half-day (4-5 hrs)", "Full-day (8-10 hrs)", "Weekend (2 days)", "3+ days"
    private String budget;            // e.g. "Budget (₹500 - ₹1,500)", "Moderate (₹1,500 - ₹3,500)", "Premium (₹3,500+)"
    private List<String> interests = new ArrayList<>();           // e.g. "Heritage & Culture", "Scenic Nature", "Beaches", "Tech Hubs", "Shopping"
    private List<String> foodPreferences = new ArrayList<>();     // e.g. "Kerala Traditional & Seafood", "Vegetarian", "Cafes & Bakeries", "Street Food"
    private List<String> activityPreferences = new ArrayList<>(); // e.g. "Sightseeing", "Relaxed & Leisurely", "Photography", "Waterfront Cruise"

    public CopilotTripRequest() {}

    public CopilotTripRequest(int passengerId, String startingLocation, String tripPurpose, String destination, String duration, String budget) {
        this.passengerId = passengerId;
        this.startingLocation = startingLocation;
        this.tripPurpose = tripPurpose;
        this.destination = destination;
        this.duration = duration;
        this.budget = budget;
    }

    public CopilotTripRequest(int passengerId, String tripPurpose, String destination, String duration, String budget) {
        this(passengerId, null, tripPurpose, destination, duration, budget);
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getTripPurpose() {
        return tripPurpose;
    }

    public void setTripPurpose(String tripPurpose) {
        this.tripPurpose = tripPurpose;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public List<String> getInterests() {
        return interests;
    }

    public void setInterests(List<String> interests) {
        this.interests = interests != null ? interests : new ArrayList<>();
    }

    public List<String> getFoodPreferences() {
        return foodPreferences;
    }

    public void setFoodPreferences(List<String> foodPreferences) {
        this.foodPreferences = foodPreferences != null ? foodPreferences : new ArrayList<>();
    }

    public List<String> getActivityPreferences() {
        return activityPreferences;
    }

    public void setActivityPreferences(List<String> activityPreferences) {
        this.activityPreferences = activityPreferences != null ? activityPreferences : new ArrayList<>();
    }

    public String getStartingLocation() {
        return startingLocation;
    }

    public void setStartingLocation(String startingLocation) {
        this.startingLocation = startingLocation;
    }
}
