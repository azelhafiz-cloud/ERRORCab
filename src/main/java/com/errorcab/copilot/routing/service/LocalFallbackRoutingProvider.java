package com.errorcab.copilot.routing.service;

import com.errorcab.copilot.routing.model.RouteResult;

/**
 * Local offline fallback routing provider.
 * Uses great-circle Haversine formula combined with Indian road circuity factor (1.30x)
 * and tiered transit speed modeling. Never depends on external network connectivity.
 */
public class LocalFallbackRoutingProvider implements RoutingProvider {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double ROAD_CIRCUITY_FACTOR = 1.30;

    @Override
    public String getProviderName() {
        return "ERRORCab Local Coordinate Routing (Offline)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public RouteResult calculateRoute(double startLat, double startLon,
                                       double endLat, double endLon,
                                       String originName, String destName) {
        if (startLat == 0.0 && startLon == 0.0 && endLat == 0.0 && endLon == 0.0) {
            return RouteResult.unavailable(originName, destName, "Invalid origin and destination coordinates (0, 0).");
        }

        // Direct Haversine calculation
        double dLat = Math.toRadians(endLat - startLat);
        double dLon = Math.toRadians(endLon - startLon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(startLat)) * Math.cos(Math.toRadians(endLat))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double directKm = EARTH_RADIUS_KM * c;

        // Apply realistic Indian road circuity factor
        double roadDistKm = Math.max(2.5, Math.round((directKm * ROAD_CIRCUITY_FACTOR) * 10.0) / 10.0);

        // Tiered speed modeling: Urban (<20km) ~ 24 km/h, Suburban (20-75km) ~ 40 km/h, Intercity/Highway (>75km) ~ 60 km/h
        double avgSpeedKmh;
        if (roadDistKm <= 20.0) {
            avgSpeedKmh = 24.0;
        } else if (roadDistKm <= 75.0) {
            avgSpeedKmh = 40.0;
        } else {
            avgSpeedKmh = 60.0;
        }

        int durationMins = (int) Math.max(8, Math.round((roadDistKm / avgSpeedKmh) * 60.0));

        return new RouteResult(
                originName != null ? originName : "Origin",
                destName != null ? destName : "Destination",
                startLat, startLon, endLat, endLon,
                roadDistKm, durationMins, true, true,
                "LOCAL_HAVERSINE_ESTIMATE",
                "Estimated road distance calculated via geographical coordinates (offline fallback)."
        );
    }
}
