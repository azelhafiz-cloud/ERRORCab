package com.errorcab.model;

/**
 * Passenger entity inheriting from User.
 * Demonstrates Inheritance and Polymorphism.
 */
public class Passenger extends User {
    private String defaultAddress;
    private int totalRides;
    private double totalSpent;

    public Passenger(int id, String name, String email, String phone, String password, boolean active,
                     String defaultAddress, int totalRides, double totalSpent) {
        super(id, name, email, phone, password, Role.PASSENGER, active);
        this.defaultAddress = defaultAddress;
        this.totalRides = totalRides;
        this.totalSpent = totalSpent;
    }

    public Passenger(String name, String email, String phone, String password) {
        super(name, email, phone, password, Role.PASSENGER);
        this.defaultAddress = "Kakkanad, Kochi";
        this.totalRides = 0;
        this.totalSpent = 0.0;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public int getTotalRides() {
        return totalRides;
    }

    public void setTotalRides(int totalRides) {
        this.totalRides = totalRides;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    @Override
    public String getWelcomeSubtitle() {
        return "Where are you going today?";
    }
}
