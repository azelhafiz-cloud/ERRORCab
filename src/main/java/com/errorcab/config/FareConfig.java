package com.errorcab.config;

/**
 * Pricing parameters for Indian Cab Categories.
 * Easily configurable parameters as required by Section 3 of specifications.
 */
public class FareConfig {
    // ECONOMY Cab
    public static final double ECONOMY_BASE_FARE = 50.0;
    public static final double ECONOMY_PER_KM = 14.0;
    public static final double ECONOMY_MIN_FARE = 80.0;

    // PREMIUM Cab
    public static final double PREMIUM_BASE_FARE = 80.0;
    public static final double PREMIUM_PER_KM = 20.0;
    public static final double PREMIUM_MIN_FARE = 120.0;

    // SUV Cab
    public static final double SUV_BASE_FARE = 100.0;
    public static final double SUV_PER_KM = 25.0;
    public static final double SUV_MIN_FARE = 150.0;
}
