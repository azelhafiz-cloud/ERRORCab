package com.errorcab.copilot.model;

import com.errorcab.copilot.destination.model.DestinationProfile;
import com.errorcab.copilot.destination.model.SafetyAdvisory;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Result structure returned by ERRORCab AI Travel Copilot.
 * Contains personalized itinerary, destination intelligence profile,
 * verified safety advisories, curated culinary highlights, budget breakdown,
 * smart day balance, and actionable ERRORCab rides.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
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
    private List<String> specialties = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<CopilotRideSuggestion> recommendedRides = new ArrayList<>();
    private double estimatedTotalCabFare;
    private String providerName;
    private LocalDateTime generatedAt;

    // Enhanced Intelligence & UX Fields
    private DestinationProfile destinationProfile;
    private CuratedCulinaryInfo curatedCulinary;
    private BudgetBreakdown budgetBreakdown;
    private DayBalance dayBalance;
    private List<String> tripReadiness = new ArrayList<>();
    private String tripExplanation;
    private String assistanceType = "SMART_OFFLINE"; // "AI_ASSISTED" or "SMART_OFFLINE"
    private String weatherNote;
    private List<SafetyAdvisory> safetyAdvisories = new ArrayList<>();

    // India-wide Destination Intelligence & Routing
    private com.errorcab.copilot.destination.model.DestinationResult destinationResult;
    private com.errorcab.copilot.routing.model.RouteResult routeResult;
    private DriverRecommendation recommendedDriver;
    private com.errorcab.copilot.weather.model.WeatherResult weather;
    private double economyFare;
    private double premiumFare;
    private double suvFare;
    private boolean destinationResolved = true;
    private String resolutionErrorMessage;

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

    public DestinationProfile getDestinationProfile() {
        return destinationProfile;
    }

    public void setDestinationProfile(DestinationProfile destinationProfile) {
        this.destinationProfile = destinationProfile;
    }

    public CuratedCulinaryInfo getCuratedCulinary() {
        return curatedCulinary;
    }

    public void setCuratedCulinary(CuratedCulinaryInfo curatedCulinary) {
        this.curatedCulinary = curatedCulinary;
    }

    public BudgetBreakdown getBudgetBreakdown() {
        return budgetBreakdown;
    }

    public void setBudgetBreakdown(BudgetBreakdown budgetBreakdown) {
        this.budgetBreakdown = budgetBreakdown;
    }

    public DayBalance getDayBalance() {
        return dayBalance;
    }

    public void setDayBalance(DayBalance dayBalance) {
        this.dayBalance = dayBalance;
    }

    public List<String> getTripReadiness() {
        return tripReadiness;
    }

    public void setTripReadiness(List<String> tripReadiness) {
        this.tripReadiness = tripReadiness != null ? tripReadiness : new ArrayList<>();
    }

    public String getTripExplanation() {
        return tripExplanation;
    }

    public void setTripExplanation(String tripExplanation) {
        this.tripExplanation = tripExplanation;
    }

    public String getAssistanceType() {
        return assistanceType;
    }

    public void setAssistanceType(String assistanceType) {
        this.assistanceType = assistanceType;
    }

    public String getWeatherNote() {
        return weatherNote;
    }

    public void setWeatherNote(String weatherNote) {
        this.weatherNote = weatherNote;
    }

    public List<SafetyAdvisory> getSafetyAdvisories() {
        return safetyAdvisories;
    }

    public void setSafetyAdvisories(List<SafetyAdvisory> safetyAdvisories) {
        this.safetyAdvisories = safetyAdvisories != null ? safetyAdvisories : new ArrayList<>();
    }

    public com.errorcab.copilot.destination.model.DestinationResult getDestinationResult() {
        return destinationResult;
    }

    public void setDestinationResult(com.errorcab.copilot.destination.model.DestinationResult destinationResult) {
        this.destinationResult = destinationResult;
    }

    public com.errorcab.copilot.routing.model.RouteResult getRouteResult() {
        return routeResult;
    }

    public void setRouteResult(com.errorcab.copilot.routing.model.RouteResult routeResult) {
        this.routeResult = routeResult;
    }

    public DriverRecommendation getRecommendedDriver() {
        return recommendedDriver;
    }

    public void setRecommendedDriver(DriverRecommendation recommendedDriver) {
        this.recommendedDriver = recommendedDriver;
    }

    public com.errorcab.copilot.weather.model.WeatherResult getWeather() {
        return weather;
    }

    public void setWeather(com.errorcab.copilot.weather.model.WeatherResult weather) {
        this.weather = weather;
    }

    public double getEconomyFare() {
        return economyFare;
    }

    public void setEconomyFare(double economyFare) {
        this.economyFare = economyFare;
    }

    public double getPremiumFare() {
        return premiumFare;
    }

    public void setPremiumFare(double premiumFare) {
        this.premiumFare = premiumFare;
    }

    public double getSuvFare() {
        return suvFare;
    }

    public void setSuvFare(double suvFare) {
        this.suvFare = suvFare;
    }

    public boolean isDestinationResolved() {
        return destinationResolved;
    }

    public void setDestinationResolved(boolean destinationResolved) {
        this.destinationResolved = destinationResolved;
    }

    public String getResolutionErrorMessage() {
        return resolutionErrorMessage;
    }

    public void setResolutionErrorMessage(String resolutionErrorMessage) {
        this.resolutionErrorMessage = resolutionErrorMessage;
    }
}
