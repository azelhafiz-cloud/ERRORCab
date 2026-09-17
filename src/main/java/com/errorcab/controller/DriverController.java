package com.errorcab.controller;

import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.Driver;
import com.errorcab.model.RideStatus;
import com.errorcab.service.DriverService;
import com.errorcab.service.RideService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Driver Dashboard, Status Toggling, and Dispatch Handling.
 */
@RestController
@RequestMapping("/api/drivers")
public class DriverController {
    private final DriverService driverService;
    private final RideService rideService = RideService.getInstance();

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDriver(@PathVariable int id) {
        return driverService.getDriverProfile(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/profile")
    public ResponseEntity<?> getProfile(@PathVariable int id) {
        return getDriver(id);
    }

    @PutMapping("/{id}/availability")
    public ResponseEntity<?> setAvailability(@PathVariable int id, @RequestBody Map<String, Boolean> body) {
        boolean online = body.getOrDefault("online", true);
        boolean ok = driverService.setAvailability(id, online);
        return ResponseEntity.ok(Map.of("success", ok, "online", online));
    }

    @PostMapping("/{id}/toggle-availability")
    public ResponseEntity<?> toggleAvailability(@PathVariable int id) {
        Optional<Driver> driverOpt = driverService.getDriverProfile(id);
        if (driverOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        boolean newStatus = !driverOpt.get().isAvailable();
        boolean ok = driverService.setAvailability(id, newStatus);
        return ResponseEntity.ok(Map.of("success", ok, "isAvailable", newStatus));
    }

    @GetMapping("/{id}/requests")
    public ResponseEntity<?> getPendingRequests(@PathVariable int id) {
        Optional<Driver> driverOpt = driverService.getDriverProfile(id);
        if (driverOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Driver driver = driverOpt.get();
        if (!driver.isOnline()) {
            return ResponseEntity.ok(List.of());
        }

        CabType type = driver.getVehicle() != null ? driver.getVehicle().getCabType() : CabType.ECONOMY;
        List<Booking> pending = rideService.getPendingRequests(type);
        return ResponseEntity.ok(pending);
    }

    @GetMapping("/available-rides")
    public ResponseEntity<?> getAllAvailableRides(@RequestParam(required = false) Integer driverId) {
        if (driverId != null) {
            Optional<Driver> dOpt = driverService.getDriverProfile(driverId);
            CabType type = dOpt.map(d -> d.getVehicle() != null ? d.getVehicle().getCabType() : CabType.ECONOMY).orElse(CabType.ECONOMY);
            return ResponseEntity.ok(rideService.getPendingRequests(type, driverId));
        }
        return ResponseEntity.ok(rideService.getPendingRequests(CabType.ECONOMY));
    }

    @GetMapping("/{id}/earnings")
    public ResponseEntity<?> getDriverEarnings(@PathVariable int id) {
        try {
            return ResponseEntity.ok(driverService.getDriverEarningsAnalytics(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{driverId}/rides/{bookingId}/status")
    public ResponseEntity<?> updateRideStatus(
            @PathVariable int driverId,
            @PathVariable int bookingId,
            @RequestBody Map<String, String> body) {
        try {
            RideStatus target = RideStatus.valueOf(body.get("status").toUpperCase().trim());
            String otp = body.get("otp");
            boolean ok = driverService.updateRideStatus(driverId, bookingId, target, otp);
            return ResponseEntity.ok(Map.of("success", ok, "status", target.name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<?> acceptRideLegacy(@PathVariable int id, @RequestBody Map<String, Integer> body) {
        try {
            int bookingId = body.get("bookingId");
            boolean ok = driverService.acceptRide(id, bookingId);
            return ResponseEntity.ok(Map.of("success", ok, "bookingId", bookingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/accept/{bookingId}")
    public ResponseEntity<?> acceptRide(
            @PathVariable int bookingId,
            @RequestParam(required = false) Integer driverId,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            int dId = driverId != null ? driverId : (body != null && body.containsKey("driverId") ? Integer.parseInt(body.get("driverId").toString()) : 2);
            boolean ok = driverService.acceptRide(dId, bookingId);
            return ResponseEntity.ok(Map.of("success", ok, "bookingId", bookingId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reject/{bookingId}")
    public ResponseEntity<?> rejectRide(
            @PathVariable int bookingId,
            @RequestParam(required = false) Integer driverId,
            @RequestBody(required = false) Map<String, Object> body) {
        try {
            int dId = driverId != null ? driverId : (body != null && body.containsKey("driverId") ? Integer.parseInt(body.get("driverId").toString()) : 2);
            Map<String, Object> res = driverService.declineRide(dId, bookingId);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
