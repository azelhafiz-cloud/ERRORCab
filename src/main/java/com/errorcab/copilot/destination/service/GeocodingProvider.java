package com.errorcab.copilot.destination.service;

import com.errorcab.copilot.destination.model.DestinationResult;

/**
 * Abstraction for geocoding arbitrary destination strings into
 * geographical coordinates and administrative entities.
 */
public interface GeocodingProvider {

    /**
     * Attempts to geocode a place name into a DestinationResult.
     *
     * @param placeName raw user input (e.g. "Perinthalmanna", "Jaipur, Rajasthan")
     * @return DestinationResult with coordinates if resolved, or marked unresolved if not found.
     */
    DestinationResult geocode(String placeName);

    /**
     * Human-readable provider name.
     */
    String getProviderName();

    /**
     * Whether this geocoder is available / enabled.
     */
    boolean isAvailable();
}
