package com.errorcab.copilot.weather.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Result representing factual weather conditions at a destination.
 * Never fabricated.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeatherResult {

    private double temperatureC;
    private double precipitationMm;
    private String condition; // e.g. "Sunny", "Rainy", "Overcast", "Clear"
    private boolean raining;
    private boolean available;
    private String source;

    public WeatherResult() {}

    public WeatherResult(double temperatureC, double precipitationMm, String condition, boolean raining, String source) {
        this.temperatureC = temperatureC;
        this.precipitationMm = precipitationMm;
        this.condition = condition;
        this.raining = raining;
        this.available = true;
        this.source = source != null ? source : "Open-Meteo Weather API";
    }

    public static WeatherResult unavailable() {
        WeatherResult w = new WeatherResult();
        w.setAvailable(false);
        return w;
    }

    public double getTemperatureC() {
        return temperatureC;
    }

    public void setTemperatureC(double temperatureC) {
        this.temperatureC = temperatureC;
    }

    public double getPrecipitationMm() {
        return precipitationMm;
    }

    public void setPrecipitationMm(double precipitationMm) {
        this.precipitationMm = precipitationMm;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public boolean isRaining() {
        return raining;
    }

    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
