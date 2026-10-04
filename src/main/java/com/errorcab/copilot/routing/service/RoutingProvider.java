package com.errorcab.copilot.routing.service;

import com.errorcab.copilot.routing.model.RouteResult;

/**
 * Abstraction for road routing and distance calculations across India.
 */
public interface RoutingProvider {

    /**
     * Calculates road distance, travel duration, and route feasibility between two coordinates.
     */
    RouteResult calculateRoute(double startLat, double startLon,
                               double endLat, double endLon,
                               String originName, String destName);

    /**
     * Provider identification name.
     */
    String getProviderName();

    /**
     * Provider availability status.
     */
    boolean isAvailable();
}
