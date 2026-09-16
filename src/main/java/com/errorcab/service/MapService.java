package com.errorcab.service;

import com.errorcab.model.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service managing local Indian locations, distance estimation,
 * and coordinate waypoints for route simulation.
 * Works completely offline without external map APIs.
 */
public class MapService {
    private static MapService instance;
    private final Map<String, Location> locations = new LinkedHashMap<>();
    private final Map<String, Double> distanceMatrix = new HashMap<>();

    private MapService() {
        initLocations();
        initDistances();
    }

    public static synchronized MapService getInstance() {
        if (instance == null) {
            instance = new MapService();
        }
        return instance;
    }

    private void initLocations() {
        // Kochi / Ernakulam local landmark coordinates mapped onto a 600x400 Canvas
        addLocation(new Location("Kakkanad", "Ernakulam", "Infopark IT Hub", 460, 140, 10.0159, 76.3419));
        addLocation(new Location("Edappally", "Ernakulam", "Lulu Mall & Metro", 340, 90, 10.0261, 76.3125));
        addLocation(new Location("Palarivattom", "Ernakulam", "Civil Line Junction", 330, 150, 10.0034, 76.3075));
        addLocation(new Location("Kaloor", "Ernakulam", "JLN International Stadium", 290, 190, 9.9932, 76.2934));
        addLocation(new Location("Vyttila", "Ernakulam", "Mobility Hub", 360, 240, 9.9678, 76.3184));
        addLocation(new Location("MG Road", "Ernakulam", "Commercial Boulevard", 220, 230, 9.9723, 76.2825));
        addLocation(new Location("Ernakulam South", "Ernakulam", "Railway Junction", 240, 270, 9.9654, 76.2891));
        addLocation(new Location("Thrippunithura", "Ernakulam", "Hill Palace", 450, 290, 9.9515, 76.3508));
        addLocation(new Location("Fort Kochi", "Ernakulam", "Chinese Fishing Nets", 90, 260, 9.9658, 76.2421));
        addLocation(new Location("Aluva", "Ernakulam", "Periyar River & Metro", 370, 30, 10.1076, 76.3516));
        
        // Extended Indian Hubs
        addLocation(new Location("Trivandrum", "Kerala", "Technopark / Central", 300, 370, 8.5241, 76.9366));
        addLocation(new Location("Kozhikode", "Kerala", "Beach / Mavoor Road", 180, 40, 11.2588, 75.7804));
        addLocation(new Location("Bengaluru", "Karnataka", "MG Road / Koramangala", 520, 50, 12.9716, 77.5946));
        addLocation(new Location("Chennai", "Tamil Nadu", "T. Nagar / Central", 550, 110, 13.0827, 80.2707));
    }

    private void addLocation(Location loc) {
        locations.put(loc.getName(), loc);
    }

    private void initDistances() {
        // Predefined benchmark distances (km)
        setDistance("Kakkanad", "Vyttila", 9.2);
        setDistance("Kakkanad", "Edappally", 7.5);
        setDistance("Edappally", "Vyttila", 6.8);
        setDistance("Edappally", "Fort Kochi", 16.5);
        setDistance("Kakkanad", "Fort Kochi", 18.2);
        setDistance("Aluva", "MG Road", 20.0);
        setDistance("Aluva", "Kakkanad", 14.0);
        setDistance("Aluva", "Edappally", 12.5);
        setDistance("Kaloor", "MG Road", 3.8);
        setDistance("Kaloor", "Vyttila", 5.2);
        setDistance("Palarivattom", "Vyttila", 4.5);
        setDistance("MG Road", "Fort Kochi", 11.5);
        setDistance("Vyttila", "Thrippunithura", 5.5);
        setDistance("Kakkanad", "Thrippunithura", 11.0);
        setDistance("Ernakulam South", "MG Road", 2.0);
        setDistance("Ernakulam South", "Vyttila", 4.8);
    }

    private void setDistance(String from, String to, double km) {
        distanceMatrix.put(from.toLowerCase() + "->" + to.toLowerCase(), km);
        distanceMatrix.put(to.toLowerCase() + "->" + from.toLowerCase(), km);
    }

    public List<Location> getAllLocations() {
        return new ArrayList<>(locations.values());
    }

    public Optional<Location> getLocation(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(locations.get(name));
    }

    public double getDistanceKm(String from, String to) {
        if (from == null || to == null) return 5.0;
        if (from.equalsIgnoreCase(to)) return 1.5;

        String key = from.toLowerCase() + "->" + to.toLowerCase();
        if (distanceMatrix.containsKey(key)) {
            return distanceMatrix.get(key);
        }

        // Coordinate-based geometric fallback if not directly in matrix
        Location locFrom = locations.get(from);
        Location locTo = locations.get(to);
        if (locFrom != null && locTo != null) {
            double dx = locTo.getMapX() - locFrom.getMapX();
            double dy = locTo.getMapY() - locFrom.getMapY();
            double pixelDist = Math.sqrt(dx * dx + dy * dy);
            double estimatedKm = Math.round((pixelDist / 22.0) * 10.0) / 10.0;
            return Math.max(3.0, estimatedKm);
        }

        return 8.0;
    }

    public int getEstimatedMinutes(double distanceKm) {
        // Average Indian urban transit speed ~ 22-25 km/h + 2 min buffer
        int minutes = (int) Math.round((distanceKm / 23.0) * 60.0);
        return Math.max(5, minutes);
    }

    /**
     * Generates a realistic route path with intermediate bend waypoints
     * between pickup and destination for smooth vehicle animation.
     */
    public List<double[]> getRouteWaypoints(String from, String to) {
        Location p1 = locations.get(from);
        Location p2 = locations.get(to);
        if (p1 == null || p2 == null) {
            List<double[]> fallback = new ArrayList<>();
            fallback.add(new double[]{200, 200});
            fallback.add(new double[]{400, 250});
            return fallback;
        }

        List<double[]> points = new ArrayList<>();
        points.add(new double[]{p1.getMapX(), p1.getMapY()});

        // Add 1 or 2 realistic city junction turns
        double midX = (p1.getMapX() + p2.getMapX()) / 2.0;
        double midY = (p1.getMapY() + p2.getMapY()) / 2.0;
        double jitterX = (p2.getMapY() - p1.getMapY()) * 0.15;
        double jitterY = (p1.getMapX() - p2.getMapX()) * 0.15;

        points.add(new double[]{midX + jitterX, midY + jitterY});
        points.add(new double[]{p2.getMapX(), p2.getMapY()});

        return points;
    }
}
