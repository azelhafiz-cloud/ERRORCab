package com.errorcab.model;

import com.errorcab.config.AppConfig;

/**
 * Abstract class representing a Cab category.
 * Demonstrates:
 * - Abstraction: Outlines the base attributes and calculateFare abstract method.
 * - Polymorphism: Each subclass implements calculateFare differently based on specific pricing rules.
 */
public abstract class Cab {
    protected CabType type;
    protected double baseFare;
    protected double perKmRate;
    protected double minimumFare;

    public Cab(CabType type, double baseFare, double perKmRate, double minimumFare) {
        this.type = type;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.minimumFare = minimumFare;
    }

    public CabType getType() {
        return type;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getPerKmRate() {
        return perKmRate;
    }

    public double getMinimumFare() {
        return minimumFare;
    }

    /**
     * Polymorphic method to calculate the fare for a given distance.
     * Formula: Fare = Base Fare + (Distance * Price Per KM)
     * If the result is lower than the minimum fare, use the minimum fare.
     * Round the final amount to the nearest ₹1.
     *
     * @param distanceKm Distance travelled in kilometers
     * @return Calculated and rounded fare amount in INR
     */
    public abstract double calculateFare(double distanceKm);

    /**
     * Formats the fare breakdown into a user-friendly string.
     */
    public String getFareBreakdown(double distanceKm) {
        double raw = baseFare + (distanceKm * perKmRate);
        double finalFare = calculateFare(distanceKm);
        boolean appliedMin = raw < minimumFare;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Base Fare: %s%.0f\n", AppConfig.CURRENCY_SYMBOL, baseFare));
        sb.append(String.format("Distance Fare: (%.1f km × %s%.0f/km) = %s%.1f\n",
                distanceKm, AppConfig.CURRENCY_SYMBOL, perKmRate, AppConfig.CURRENCY_SYMBOL, (distanceKm * perKmRate)));
        sb.append(String.format("Subtotal: %s%.1f\n", AppConfig.CURRENCY_SYMBOL, raw));
        if (appliedMin) {
            sb.append(String.format("Minimum Fare Applied: %s%.0f\n", AppConfig.CURRENCY_SYMBOL, minimumFare));
        }
        sb.append(String.format("Final Fare: %s%.0f", AppConfig.CURRENCY_SYMBOL, finalFare));
        return sb.toString();
    }
}
