package com.errorcab.model;

import com.errorcab.config.FareConfig;

/**
 * Premium Cab implementation.
 * Rates: Base ₹80, Per km ₹20, Min ₹120.
 * Demonstrates Polymorphism.
 */
public class PremiumCab extends Cab {

    public PremiumCab() {
        super(CabType.PREMIUM, FareConfig.PREMIUM_BASE_FARE, FareConfig.PREMIUM_PER_KM, FareConfig.PREMIUM_MIN_FARE);
    }

    @Override
    public double calculateFare(double distanceKm) {
        double calculated = baseFare + (distanceKm * perKmRate);
        double result = Math.max(calculated, minimumFare);
        return Math.round(result);
    }
}
