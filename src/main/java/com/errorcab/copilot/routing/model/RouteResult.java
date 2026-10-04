package com.errorcab.copilot.routing.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Result representing road routing between two geographic points in India.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RouteResult {

    private String originName;
    private String destinationName;
    private double originLatitude;
    private double originLongitude;
    private double destinationLatitude;
    private double destinationLongitude;
    private double distanceKm;
    private int durationMinutes;
    private String durationFormatted;
    private boolean routeAvailable;
    private boolean fallbackEstimate;
    private String calculationMethod; // "OSRM_ONLINE_ROUTING", "LOCAL_HAVERSINE_ESTIMATE", "MATRIX_BENCHMARK"
    private String statusMessage;

    public RouteResult() {}

    public RouteResult(String originName, String destinationName,
                       double originLatitude, double originLongitude,
                       double destinationLatitude, double destinationLongitude,
                       double distanceKm, int durationMinutes,
                       boolean routeAvailable, boolean fallbackEstimate,
                       String calculationMethod, String statusMessage) {
        this.originName = originName;
        this.destinationName = destinationName;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.distanceKm = Math.round(distanceKm * 10.0) / 10.0;
        this.durationMinutes = durationMinutes;
        this.durationFormatted = formatDuration(durationMinutes);
        this.routeAvailable = routeAvailable;
        this.fallbackEstimate = fallbackEstimate;
        this.calculationMethod = calculationMethod;
        this.statusMessage = statusMessage;
    }

    public static RouteResult unavailable(String originName, String destinationName, String reason) {
        RouteResult r = new RouteResult();
        r.setOriginName(originName);
        r.setDestinationName(destinationName);
        r.setRouteAvailable(false);
        r.setStatusMessage(reason != null ? reason : "Route calculation unavailable between specified locations.");
        r.setCalculationMethod("UNAVAILABLE");
        return r;
    }

    private static String formatDuration(int minutes) {
        if (minutes <= 0) return "5 mins";
        if (minutes < 60) return minutes + " mins";
        int hrs = minutes / 60;
        int remMins = minutes % 60;
        if (remMins == 0) return hrs + " hr" + (hrs > 1 ? "s" : "");
        return hrs + "h " + remMins + "m";
    }

    public String getOriginName() {
        return originName;
    }

    public void setOriginName(String originName) {
        this.originName = originName;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = Math.round(distanceKm * 10.0) / 10.0;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
        this.durationFormatted = formatDuration(durationMinutes);
    }

    public String getDurationFormatted() {
        return durationFormatted != null ? durationFormatted : formatDuration(durationMinutes);
    }

    public void setDurationFormatted(String durationFormatted) {
        this.durationFormatted = durationFormatted;
    }

    public boolean isRouteAvailable() {
        return routeAvailable;
    }

    public void setRouteAvailable(boolean routeAvailable) {
        this.routeAvailable = routeAvailable;
    }

    public boolean isFallbackEstimate() {
        return fallbackEstimate;
    }

    public void setFallbackEstimate(boolean fallbackEstimate) {
        this.fallbackEstimate = fallbackEstimate;
    }

    public String getCalculationMethod() {
        return calculationMethod;
    }

    public String getMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(String calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
