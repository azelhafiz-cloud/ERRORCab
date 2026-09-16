package com.errorcab.model;

import com.errorcab.config.FareConfig;

/**
 * Economy Cab implementation.
 * Rates: Base ₹50, Per km ₹14, Min ₹80.
 * Demonstrates Polymorphism.
 */
public class EconomyCab extends Cab {

    public EconomyCab() {
        super(CabType.ECONOMY, FareConfig.ECONOMY_BASE_FARE, FareConfig.ECONOMY_PER_KM, FareConfig.ECONOMY_MIN_FARE);
    }

    @Override
    public double calculateFare(double distanceKm) {
        double calculated = baseFare + (distanceKm * perKmRate);
        double result = Math.max(calculated, minimumFare);
        return Math.round(result);
    }
}
