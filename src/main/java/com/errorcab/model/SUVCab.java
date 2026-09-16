package com.errorcab.model;

import com.errorcab.config.FareConfig;

/**
 * SUV Cab implementation.
 * Rates: Base ₹100, Per km ₹25, Min ₹150.
 * Demonstrates Polymorphism.
 */
public class SUVCab extends Cab {

    public SUVCab() {
        super(CabType.SUV, FareConfig.SUV_BASE_FARE, FareConfig.SUV_PER_KM, FareConfig.SUV_MIN_FARE);
    }

    @Override
    public double calculateFare(double distanceKm) {
        double calculated = baseFare + (distanceKm * perKmRate);
        double result = Math.max(calculated, minimumFare);
        return Math.round(result);
    }
}
