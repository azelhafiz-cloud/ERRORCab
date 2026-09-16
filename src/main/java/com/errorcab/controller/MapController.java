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
}
