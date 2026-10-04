package com.errorcab.copilot.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * DTO representing an authentic ERRORCab driver recommendation
 * ranked by real database attributes (online status, rating, completed rides).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DriverRecommendation {

    private int driverId;
    private String driverName;
    private double rating;
    private int completedRides;
    private String vehicleModel;
    private String plateNumber;
    private String cabType;
    private String color;
    private String currentLocation;
    private String estimatedPickupDistance;
    private boolean available;
    private String statusMessage;

    public DriverRecommendation() {}

    public static DriverRecommendation noneAvailable(String message) {
        DriverRecommendation d = new DriverRecommendation();
        d.setAvailable(false);
        d.setStatusMessage(message != null ? message : "No ERRORCab driver is currently available.");
        return d;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getCompletedRides() {
        return completedRides;
    }

    public void setCompletedRides(int completedRides) {
        this.completedRides = completedRides;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getCabType() {
        return cabType;
    }

    public void setCabType(String cabType) {
        this.cabType = cabType;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getEstimatedPickupDistance() {
        return estimatedPickupDistance;
    }

    public void setEstimatedPickupDistance(String estimatedPickupDistance) {
        this.estimatedPickupDistance = estimatedPickupDistance;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
