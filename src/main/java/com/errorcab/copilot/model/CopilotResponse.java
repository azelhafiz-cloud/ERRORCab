package com.errorcab.copilot.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result structure returned by ERRORCab AI Travel Copilot.
 * Contains personalized itinerary, dining spots, local tips, and actionable ERRORCab rides.
 */
public class CopilotResponse {
    private boolean success;
    private String title;
    private String summary;
    private String tripPurpose;
    private String destination;
    private String duration;
    private String budget;
    private List<ItineraryLeg> itinerary = new ArrayList<>();
    private List<String> foodRecommendations = new ArrayList<>();
    private List<String> travelTips = new ArrayList<>();
    private List<CopilotRideSuggestion> recommendedRides = new ArrayList<>();
    private double estimatedTotalCabFare;
    private String providerName;
    private LocalDateTime generatedAt;

    public CopilotResponse() {
        this.generatedAt = LocalDateTime.now();
        this.success = true;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public List<ItineraryLeg> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<ItineraryLeg> itinerary) {
        this.itinerary = itinerary != null ? itinerary : new ArrayList<>();
    }

    public List<String> getFoodRecommendations() {
        return foodRecommendations;
    }

    public void setFoodRecommendations(List<String> foodRecommendations) {
        this.foodRecommendations = foodRecommendations != null ? foodPreferences(foodRecommendations) : new ArrayList<>();
    }

    private List<String> foodPreferences(List<String> list) {
        return list != null ? list : new ArrayList<>();
    }

    public List<String> getTravelTips() {
        return travelTips;
    }

    public void setTravelTips(List<String> travelTips) {
        this.travelTips = travelTips != null ? travelTips : new ArrayList<>();
    }

    public List<CopilotRideSuggestion> getRecommendedRides() {
        return recommendedRides;
    }

    public void setRecommendedRides(List<CopilotRideSuggestion> recommendedRides) {
        this.recommendedRides = recommendedRides != null ? recommendedRides : new ArrayList<>();
    }

    public double getEstimatedTotalCabFare() {
        return estimatedTotalCabFare;
    }

    public void setEstimatedTotalCabFare(double estimatedTotalCabFare) {
        this.estimatedTotalCabFare = estimatedTotalCabFare;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
