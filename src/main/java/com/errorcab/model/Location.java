package com.errorcab.model;

/**
 * Represents a geographical landmark/hub in the primary Kerala/Kochi region.
 * Holds coordinates for map rendering and distance estimations.
 */
public class Location {
    private String name;
    private String district;
    private String landmark;
    private double mapX;
    private double mapY;
    private double latitude;
    private double longitude;

    public Location(String name, String district, String landmark, double mapX, double mapY, double latitude, double longitude) {
        this.name = name;
        this.district = district;
        this.landmark = landmark;
        this.mapX = mapX;
        this.mapY = mapY;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public String getDistrict() {
        return district;
    }

    public String getLandmark() {
        return landmark;
    }

    public double getMapX() {
        return mapX;
    }

    public double getMapY() {
        return mapY;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getFullName() {
        return name + ", " + district;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return name != null && name.equalsIgnoreCase(location.name);
    }

    @Override
    public int hashCode() {
        return name != null ? name.toLowerCase().hashCode() : 0;
    }
}
