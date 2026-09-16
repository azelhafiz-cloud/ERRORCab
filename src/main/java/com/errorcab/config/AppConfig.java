package com.errorcab.config;

/**
 * Global application configuration and constants.
 * ERRORCab - Indian Cab Ride Booking & Tracking System
 * Team: ERROR
 * Tagline: "Book Smart. Ride Safe."
 */
public class AppConfig {
    public static final String APP_NAME = "ERRORCab";
    public static final String TEAM_NAME = "ERROR";
    public static final String TAGLINE = "Book Smart. Ride Safe.";
    public static final String VERSION = "1.0.0";
    public static final String CURRENCY_SYMBOL = "₹";
    
    // SQLite Database path
    public static final String DB_FILE_NAME = "errorcab.db";
    public static final String DB_URL = "jdbc:sqlite:" + DB_FILE_NAME;
    
    // Demo account credentials
    public static final String DEMO_PASSWORD = "password123";
    public static final String DEMO_PASSENGER_EMAIL = "passenger@example.com";
    public static final String DEMO_DRIVER_EMAIL = "driver@example.com";
    public static final String DEMO_ADMIN_EMAIL = "admin@example.com";
    
    // Primary region
    public static final String PRIMARY_REGION = "Kerala / Kochi / Ernakulam";
}
