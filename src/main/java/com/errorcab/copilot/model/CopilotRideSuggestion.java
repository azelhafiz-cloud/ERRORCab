package com.errorcab.copilot.model;

import com.errorcab.model.CabType;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Represents an actionable ERRORCab ride suggestion embedded within an AI travel plan.
 * Connects directly to the existing ERRORCab booking system without separate booking mechanisms.
 */
public class CopilotRideSuggestion {
    private String legTitle;           // e.g. "Hop 1: Kakkanad Infopark -> Fort Kochi"
    private String pickupLocation;     // e.g. "Kakkanad"
    private String dropoffLocation;    // e.g. "Fort Kochi"
    private double distanceKm;
    private int estimatedMinutes;
    private CabType recommendedCabType;
    private double estimatedFare;
    private String bookingUrl;         // e.g. "/booking.html?pickup=Kakkanad&dropoff=Fort+Kochi&cabType=ECONOMY"
    private String notes;              // e.g. "Scenic ferry & heritage road via NH66"

    public CopilotRideSuggestion() {}

    public CopilotRideSuggestion(String legTitle, String pickupLocation, String dropoffLocation,
                                 double distanceKm, int estimatedMinutes, CabType recommendedCabType,
                                 double estimatedFare, String notes) {
        this.legTitle = legTitle;
        this.pickupLocation = pickupLocation;
        this.dropoffLocation = dropoffLocation;
        this.distanceKm = distanceKm;
        this.estimatedMinutes = estimatedMinutes;
        this.recommendedCabType = recommendedCabType != null ? recommendedCabType : CabType.ECONOMY;
        this.estimatedFare = estimatedFare;
        this.notes = notes;
        this.bookingUrl = buildBookingUrl(pickupLocation, dropoffLocation, this.recommendedCabType);
    }

    public static String buildBookingUrl(String pickup, String dropoff, CabType cabType) {
        String p = pickup != null ? URLEncoder.encode(pickup, StandardCharsets.UTF_8) : "";
        String d = dropoff != null ? URLEncoder.encode(dropoff, StandardCharsets.UTF_8) : "";
        String c = cabType != null ? cabType.name() : CabType.ECONOMY.name();
        return "/booking.html?pickup=" + p + "&dropoff=" + d + "&cabType=" + c;
    }

    public String getLegTitle() {
        return legTitle;
    }

    public void setLegTitle(String legTitle) {
        this.legTitle = legTitle;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
        this.bookingUrl = buildBookingUrl(this.pickupLocation, this.dropoffLocation, this.recommendedCabType);
    }

    public String getDropoffLocation() {
        return dropoffLocation;
    }

    public void setDropoffLocation(String dropoffLocation) {
        this.dropoffLocation = dropoffLocation;
        this.bookingUrl = buildBookingUrl(this.pickupLocation, this.dropoffLocation, this.recommendedCabType);
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public CabType getRecommendedCabType() {
        return recommendedCabType;
    }

    public void setRecommendedCabType(CabType recommendedCabType) {
        this.recommendedCabType = recommendedCabType;
        this.bookingUrl = buildBookingUrl(this.pickupLocation, this.dropoffLocation, this.recommendedCabType);
    }

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    public String getBookingUrl() {
        return bookingUrl;
    }

    public void setBookingUrl(String bookingUrl) {
        this.bookingUrl = bookingUrl;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
