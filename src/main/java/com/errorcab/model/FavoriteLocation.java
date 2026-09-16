package com.errorcab.model;

import java.time.LocalDateTime;

/**
 * Saved favorite location (Home, Work, Favorite) for quick booking.
 */
public class FavoriteLocation {
    private int id;
    private int passengerId;
    private String label; // "Home", "Work", "Favorite"
    private String locationName;
    private String address;
    private LocalDateTime createdAt;

    public FavoriteLocation(int id, int passengerId, String label, String locationName, String address, LocalDateTime createdAt) {
        this.id = id;
        this.passengerId = passengerId;
        this.label = label;
        this.locationName = locationName;
        this.address = address;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public FavoriteLocation(int passengerId, String label, String locationName, String address) {
        this(0, passengerId, label, locationName, address, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
