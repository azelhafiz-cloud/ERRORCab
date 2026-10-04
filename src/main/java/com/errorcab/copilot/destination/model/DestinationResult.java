package com.errorcab.copilot.destination.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Result representing a resolved Indian destination with normalized geographical
 * coordinates, administrative hierarchy, and resolution status.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DestinationResult {

    private String query;
    private String normalizedPlaceName;
    private String district;
    private String state;
    private String country;
    private double latitude;
    private double longitude;
    private String displayName;
    private boolean resolved;
    private String resolutionSource; // "KNOWLEDGE_BASE", "GEOCODER_NOMINATIM", "PUBLIC_DISCOVERY", "UNRESOLVED"
    private Map<String, Object> metadata = new HashMap<>();

    public DestinationResult() {
        this.country = "India";
    }

    public DestinationResult(String query, String normalizedPlaceName, String district,
                             String state, String country, double latitude, double longitude,
                             String displayName, boolean resolved, String resolutionSource) {
        this.query = query;
        this.normalizedPlaceName = normalizedPlaceName;
        this.district = district;
        this.state = state;
        this.country = country != null ? country : "India";
        this.latitude = latitude;
        this.longitude = longitude;
        this.displayName = displayName;
        this.resolved = resolved;
        this.resolutionSource = resolutionSource;
    }

    public static DestinationResult unresolved(String query) {
        DestinationResult result = new DestinationResult();
        result.setQuery(query);
        result.setNormalizedPlaceName(query != null ? query.trim() : "");
        result.setResolved(false);
        result.setResolutionSource("UNRESOLVED");
        result.setDisplayName(query);
        return result;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getNormalizedPlaceName() {
        return normalizedPlaceName;
    }

    public void setNormalizedPlaceName(String normalizedPlaceName) {
        this.normalizedPlaceName = normalizedPlaceName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : normalizedPlaceName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolutionSource() {
        return resolutionSource;
    }

    public void setResolutionSource(String resolutionSource) {
        this.resolutionSource = resolutionSource;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
}
