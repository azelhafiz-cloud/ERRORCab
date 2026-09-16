package com.errorcab.model;

/**
 * Three Cab categories with seat capacity and description.
 */
public enum CabType {
    ECONOMY("Economy", "Affordable & compact city rides", 4),
    PREMIUM("Premium", "Sedans with extra comfort & top drivers", 4),
    SUV("SUV", "Spacious 6-seaters for family & luggage", 6);

    private final String displayName;
    private final String description;
    private final int capacity;

    CabType(String displayName, String description, int capacity) {
        this.displayName = displayName;
        this.description = description;
        this.capacity = capacity;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCapacity() {
        return capacity;
    }
}
