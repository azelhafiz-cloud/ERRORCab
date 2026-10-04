package com.errorcab.copilot.weather.service;

import com.errorcab.copilot.weather.model.WeatherResult;

/**
 * Abstraction for destination weather lookup.
 */
public interface WeatherProvider {

    /**
     * Retrieves factual weather for given coordinates, or returns WeatherResult.unavailable().
     */
    WeatherResult getWeather(double latitude, double longitude);

    String getProviderName();

    boolean isAvailable();
}
