package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Represents an individual itinerary leg in Gemini's structured JSON output.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiLegPayload {
    private String timeSlot;
    private String title;
    private String locationName;
    private String category;
    private String description;
    private Boolean rideSuggested;
    private String pickupLocation;
    private String dropoffLocation;
    private String cabType;
    private String rideReason;

    public GeminiLegPayload() {}

    public String getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getRideSuggested() {
        return rideSuggested;
    }

    public void setRideSuggested(Boolean rideSuggested) {
        this.rideSuggested = rideSuggested;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
    }

    public String getCabType() {
        return cabType;
    }

    public void setCabType(String cabType) {
        this.cabType = cabType;
    }

    public String getRideReason() {
        return rideReason;
    }

    public void setRideReason(String rideReason) {
        this.rideReason = rideReason;
    }
}
