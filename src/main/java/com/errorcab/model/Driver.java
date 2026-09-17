package com.errorcab.model;

/**
 * Driver entity extending User.
 * Demonstrates Inheritance and Composition (has a Vehicle).
 */
public class Driver extends User {
    private String licenseNumber;
    private int vehicleId;
    private Vehicle vehicle;
    private boolean isOnline;
    private double rating;
    private int ratingCount;
    private double totalEarnings;
    private int completedRidesCount;
    private String currentLocation;
    private int requestsReceivedCount;
    private int requestsAcceptedCount;

    public Driver(int id, String name, String email, String phone, String password, boolean active,
                  String licenseNumber, int vehicleId, boolean isOnline, double rating, int ratingCount,
                  double totalEarnings, int completedRidesCount, String currentLocation,
                  int requestsReceivedCount, int requestsAcceptedCount) {
        super(id, name, email, phone, password, Role.DRIVER, active);
        this.licenseNumber = licenseNumber;
        this.vehicleId = vehicleId;
        this.isOnline = isOnline;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.totalEarnings = totalEarnings;
        this.completedRidesCount = completedRidesCount;
        this.currentLocation = currentLocation != null ? currentLocation : "Kakkanad";
        this.requestsReceivedCount = requestsReceivedCount;
        this.requestsAcceptedCount = requestsAcceptedCount;
    }

    public Driver(int id, String name, String email, String phone, String password, boolean active,
                  String licenseNumber, int vehicleId, boolean isOnline, double rating, int ratingCount,
                  double totalEarnings, int completedRidesCount, String currentLocation) {
        this(id, name, email, phone, password, active, licenseNumber, vehicleId, isOnline, rating, ratingCount,
                totalEarnings, completedRidesCount, currentLocation, 50, 48);
    }

    public Driver(String name, String email, String phone, String password, String licenseNumber) {
        super(name, email, phone, password, Role.DRIVER);
        this.licenseNumber = licenseNumber;
        this.isOnline = true;
        this.rating = 5.0;
        this.ratingCount = 1;
        this.totalEarnings = 0.0;
        this.completedRidesCount = 0;
        this.currentLocation = "Kakkanad";
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public int getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(int vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle != null) {
            this.vehicleId = vehicle.getId();
        }
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isAvailable() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setAvailable(boolean available) {
        isOnline = available;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRatingCount() {
        return ratingCount;
    }

    public void setRatingCount(int ratingCount) {
        this.ratingCount = ratingCount;
    }

    public double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public int getCompletedRidesCount() {
        return completedRidesCount;
    }

    public void setCompletedRidesCount(int completedRidesCount) {
        this.completedRidesCount = completedRidesCount;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public void recordCompletedRide(double fareAmount) {
        this.totalEarnings += fareAmount;
        this.completedRidesCount++;
    }

    @Override
    public String getWelcomeSubtitle() {
        return isOnline ? "You are ONLINE and accepting rides" : "You are currently OFFLINE";
    }

    public int getRequestsReceivedCount() {
        return requestsReceivedCount;
    }

    public void setRequestsReceivedCount(int requestsReceivedCount) {
        this.requestsReceivedCount = requestsReceivedCount;
    }

    public int getRequestsAcceptedCount() {
        return requestsAcceptedCount;
    }

    public void setRequestsAcceptedCount(int requestsAcceptedCount) {
        this.requestsAcceptedCount = requestsAcceptedCount;
    }

    public double getAcceptanceRate() {
        if (requestsReceivedCount <= 0) {
            return 100.0;
        }
        double rate = (requestsAcceptedCount * 100.0) / requestsReceivedCount;
        return Math.min(100.0, Math.max(0.0, Math.round(rate * 10.0) / 10.0));
    }
}
