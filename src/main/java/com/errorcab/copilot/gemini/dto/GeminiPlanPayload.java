package com.errorcab.copilot.gemini.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Structured schema that Gemini is required to return as JSON text.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeminiPlanPayload {
    private String title;
    private String summary;
    private List<GeminiLegPayload> itinerary = new ArrayList<>();
    private List<String> foodRecommendations = new ArrayList<>();
    private List<String> travelTips = new ArrayList<>();
    private List<String> specialties = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public GeminiPlanPayload() {}

    /**
     * Validates that the parsed Gemini payload contains all essential fields.
     * Throws IllegalArgumentException if schema validation fails.
     */
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Gemini response is missing required 'title'");
        }
        if (summary == null || summary.trim().isEmpty()) {
            throw new IllegalArgumentException("Gemini response is missing required 'summary'");
        }
        if (itinerary == null || itinerary.isEmpty()) {
            throw new IllegalArgumentException("Gemini response contains an empty 'itinerary'");
        }
        for (int i = 0; i < itinerary.size(); i++) {
            GeminiLegPayload leg = itinerary.get(i);
            if (leg == null || leg.getTitle() == null || leg.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Gemini itinerary leg " + (i + 1) + " is missing a 'title'");
            }
        }
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

    public List<GeminiLegPayload> getItinerary() {
        return itinerary;
    }

    public void setItinerary(List<GeminiLegPayload> itinerary) {
        this.itinerary = itinerary != null ? itinerary : new ArrayList<>();
    }

    public List<String> getFoodRecommendations() {
        return foodRecommendations;
    }

    public void setFoodRecommendations(List<String> foodRecommendations) {
        this.foodRecommendations = foodRecommendations != null ? foodRecommendations : new ArrayList<>();
    }

    public List<String> getTravelTips() {
        return travelTips;
    }

    public void setTravelTips(List<String> travelTips) {
        this.travelTips = travelTips != null ? travelTips : new ArrayList<>();
    }

    public List<String> getSpecialties() {
        return specialties;
    }

    public void setSpecialties(List<String> specialties) {
        this.specialties = specialties != null ? specialties : new ArrayList<>();
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings != null ? warnings : new ArrayList<>();
    }
}
