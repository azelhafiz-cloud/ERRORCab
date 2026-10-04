package com.errorcab.controller;

import com.errorcab.model.CabType;
import com.errorcab.model.Location;
import com.errorcab.service.FareService;
import com.errorcab.service.MapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Map locations, Kochi landmarks, and simulated route waypoints.
 */
@RestController
@RequestMapping("/api/map")
public class MapController {
    private final MapService mapService = MapService.getInstance();
    private final FareService fareService = FareService.getInstance();
    private final com.errorcab.copilot.destination.service.DestinationResolver destinationResolver =
            new com.errorcab.copilot.destination.service.DestinationResolver();

    @GetMapping("/locations")
    public ResponseEntity<List<Location>> getLocations() {
        return ResponseEntity.ok(mapService.getAllLocations());
    }

    @GetMapping("/distance")
    public ResponseEntity<?> getDistance(
            @RequestParam(required = false) String pickup,
            @RequestParam(required = false) String dropoff,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String p = pickup != null ? pickup : from;
        String d = dropoff != null ? dropoff : to;
        double distance = mapService.getDistanceKm(p, d);
        int minutes = mapService.getEstimatedMinutes(distance);

        return ResponseEntity.ok(Map.of(
                "pickup", p != null ? p : "",
                "dropoff", d != null ? d : "",
                "distance", distance,
                "durationMinutes", minutes
        ));
    }

    @GetMapping("/estimate-fare")
    public ResponseEntity<?> getEstimateFare(
            @RequestParam double distance,
            @RequestParam(defaultValue = "ECONOMY") String cabType) {
        try {
            CabType type = CabType.valueOf(cabType.toUpperCase().trim());
            double fare = fareService.calculateFare(type, distance);
            return ResponseEntity.ok(Map.of(
                    "cabType", type.name(),
                    "distance", distance,
                    "fare", fare
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/route")
    public ResponseEntity<?> getRoute(@RequestParam String from, @RequestParam String to) {
        double distance = mapService.getDistanceKm(from, to);
        int minutes = mapService.getEstimatedMinutes(distance);
        List<double[]> waypoints = mapService.getRouteWaypoints(from, to);

        return ResponseEntity.ok(Map.of(
                "pickup", from,
                "destination", to,
                "distanceKm", distance,
                "estimatedMinutes", minutes,
                "waypoints", waypoints
        ));
    }

    @GetMapping("/resolve")
    public ResponseEntity<?> resolveLocation(@RequestParam String query) {
        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("resolved", false, "message", "Query is required."));
        }
        var result = destinationResolver.resolveDestination(query.trim());
        if (result != null && result.isResolved()) {
            return ResponseEntity.ok(Map.of(
                    "resolved", true,
                    "name", result.getNormalizedPlaceName() != null ? result.getNormalizedPlaceName() : query.trim(),
                    "displayName", result.getDisplayName() != null ? result.getDisplayName() : query.trim(),
                    "district", result.getDistrict() != null ? result.getDistrict() : "",
                    "state", result.getState() != null ? result.getState() : "India",
                    "latitude", result.getLatitude(),
                    "longitude", result.getLongitude()
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "resolved", false,
                    "query", query.trim(),
                    "message", "Couldn't confidently locate this destination. Try adding the district or state."
            ));
        }
    }

    @GetMapping("/reverse-geocode")
    public ResponseEntity<?> reverseGeocode(
            @RequestParam double lat,
            @RequestParam(required = false) Double lon,
            @RequestParam(required = false) Double lng) {
        double actualLon = lon != null ? lon : (lng != null ? lng : 0.0);
        String bestName = "Current Location";
        String bestState = "India";
        double minDistance = Double.MAX_VALUE;

        for (Location loc : mapService.getAllLocations()) {
            if (loc.getLatitude() != 0 && loc.getLongitude() != 0) {
                double d = mapService.calculateHaversineDistanceKm(lat, actualLon, loc.getLatitude(), loc.getLongitude());
                if (d < minDistance) {
                    minDistance = d;
                    bestName = loc.getName();
                    bestState = loc.getDistrict() != null ? loc.getDistrict() : "India";
                }
            }
        }

        for (var entry : com.errorcab.copilot.destination.service.DestinationKnowledgeBase.getAll().entrySet()) {
            var prof = entry.getValue();
            if (prof.getLatitude() != 0 && prof.getLongitude() != 0) {
                double d = mapService.calculateHaversineDistanceKm(lat, actualLon, prof.getLatitude(), prof.getLongitude());
                if (d < minDistance) {
                    minDistance = d;
                    bestName = prof.getDestinationName();
                    bestState = prof.getState() != null ? prof.getState() : "India";
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "latitude", lat,
                "longitude", actualLon,
                "name", bestName,
                "locationName", bestName,
                "displayName", bestName + ", " + bestState,
                "state", bestState,
                "proximityKm", Math.round(minDistance * 10.0) / 10.0
        ));
    }
}
