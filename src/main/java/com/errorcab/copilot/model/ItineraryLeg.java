package com.errorcab.copilot.model;

/**
 * Represents a single stop or phase in an AI-generated itinerary.
 */
public class ItineraryLeg {
    private String timeSlot;           // e.g. "09:00 AM - 11:00 AM"
    private String title;              // e.g. "Heritage Stroll & Chinese Fishing Nets"
    private String locationName;       // e.g. "Fort Kochi"
    private String category;           // e.g. "Sightseeing", "Culinary", "Culture", "Relaxation", "Transit"
    private String description;        // Detailed recommendation
    private CopilotRideSuggestion rideSuggestion; // Optional connecting cab ride

    public ItineraryLeg() {}

    public ItineraryLeg(String timeSlot, String title, String locationName, String category,
                        String description, CopilotRideSuggestion rideSuggestion) {
        this.timeSlot = timeSlot;
        this.title = title;
        this.locationName = locationName;
        this.category = category;
        this.description = description;
        this.rideSuggestion = rideSuggestion;
    }

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

    public CopilotRideSuggestion getRideSuggestion() {
        return rideSuggestion;
    }

    public void setRideSuggestion(CopilotRideSuggestion rideSuggestion) {
        this.rideSuggestion = rideSuggestion;
    }
}
