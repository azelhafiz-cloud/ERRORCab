package com.errorcab.copilot.routing.service;

import com.errorcab.copilot.routing.model.RouteResult;
import com.errorcab.service.MapService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Composite routing provider that layers:
 * 1. In-memory route cache
 * 2. Online OSRM routing
 * 3. Local offline coordinate Haversine fallback with circuity multiplier
 * 4. Predefined MapService distance matrix
 */
public class CompositeRoutingProvider implements RoutingProvider {

    private static final Logger LOGGER = Logger.getLogger(CompositeRoutingProvider.class.getName());
    private static final Map<String, RouteResult> ROUTE_CACHE = new ConcurrentHashMap<>();

    private final RoutingProvider primaryProvider;
    private final RoutingProvider fallbackProvider;
    private final MapService mapService = MapService.getInstance();

    public CompositeRoutingProvider(RoutingProvider primaryProvider, RoutingProvider fallbackProvider) {
        this.primaryProvider = primaryProvider != null ? primaryProvider : new OsrmRoutingProvider();
        this.fallbackProvider = fallbackProvider != null ? fallbackProvider : new LocalFallbackRoutingProvider();
    }

    public CompositeRoutingProvider() {
        this(new OsrmRoutingProvider(), new LocalFallbackRoutingProvider());
    }

    @Override
    public String getProviderName() {
        return "ERRORCab Composite Routing (Online OSRM + Offline Coordinate Fallback)";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public RouteResult calculateRoute(double startLat, double startLon,
                                       double endLat, double endLon,
                                       String originName, String destName) {
        String cacheKey = makeCacheKey(startLat, startLon, endLat, endLon, originName, destName);
        if (ROUTE_CACHE.containsKey(cacheKey)) {
            return ROUTE_CACHE.get(cacheKey);
        }

        RouteResult result = null;

        // 1. Try Primary Online Provider (OSRM)
        if (startLat != 0.0 && startLon != 0.0 && endLat != 0.0 && endLon != 0.0) {
            try {
                result = primaryProvider.calculateRoute(startLat, startLon, endLat, endLon, originName, destName);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Primary routing provider failed: {0}", e.getMessage());
            }
        }

        // 2. Fall back to local coordinate calculation if coordinates are present
        if (result == null && startLat != 0.0 && startLon != 0.0 && endLat != 0.0 && endLon != 0.0) {
            result = fallbackProvider.calculateRoute(startLat, startLon, endLat, endLon, originName, destName);
        }

        // 3. Fall back to local MapService distance estimation if coordinates were missing
        if (result == null && originName != null && destName != null) {
            double km = mapService.getDistanceKm(originName, destName);
            int mins = mapService.getEstimatedMinutes(km);
            result = new RouteResult(
                    originName, destName,
                    startLat, startLon, endLat, endLon,
                    km, mins, true, true,
                    "MAP_SERVICE_BENCHMARK",
                    "Estimated road distance from ERRORCab local transit matrix."
            );
        }

        if (result != null) {
            ROUTE_CACHE.put(cacheKey, result);
            return result;
        }

        return RouteResult.unavailable(originName, destName, "Unable to calculate route between specified locations.");
    }

    private String makeCacheKey(double lat1, double lon1, double lat2, double lon2, String from, String to) {
        return String.format("%.3f,%.3f->%.3f,%.3f|%s->%s",
                lat1, lon1, lat2, lon2,
                from != null ? from.toLowerCase() : "",
                to != null ? to.toLowerCase() : "");
    }

    public static void clearCache() {
        ROUTE_CACHE.clear();
    }
}
