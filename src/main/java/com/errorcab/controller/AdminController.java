package com.errorcab.controller;

import com.errorcab.model.Booking;
import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.RideStatus;
import com.errorcab.model.Role;
import com.errorcab.model.User;
import com.errorcab.repository.DriverRepository;
import com.errorcab.repository.UserRepository;
import com.errorcab.service.AuthenticationService;
import com.errorcab.service.PaymentService;
import com.errorcab.service.RideService;
import com.errorcab.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Admin Dashboard, Analytics, User Management, and Fleet Audit.
 * Enforces server-side authentication and role-based access control (ADMIN only).
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepo = new UserRepository();
    private final DriverRepository driverRepo = new DriverRepository();
    private final RideService rideService = RideService.getInstance();
    private final PaymentService paymentService = PaymentService.getInstance();
    private final SessionService sessionService;
    private final AuthenticationService authService;

    public AdminController(SessionService sessionService, AuthenticationService authService) {
        this.sessionService = sessionService;
        this.authService = authService;
    }

    private ResponseEntity<?> checkAdminAuth(HttpServletRequest request) {
        var sessionOpt = sessionService.getSessionFromRequest(request);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication required. Please log in as an administrator."));
        }
        if (sessionOpt.get().role() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access denied. Administrator privileges required."));
        }
        return null;
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getStats(HttpServletRequest request) {
        ResponseEntity<?> authCheck = checkAdminAuth(request);
        if (authCheck != null) return authCheck;

        List<User> allUsers = userRepo.getAllUsers();
        long passengerCount = allUsers.stream().filter(u -> u.getRole() == Role.PASSENGER).count();
        long driverCount = allUsers.stream().filter(u -> u.getRole() == Role.DRIVER).count();

        List<Booking> allBookings = rideService.getAllBookings();
        long totalRides = allBookings.size();
        long completedRides = allBookings.stream().filter(b -> b.getStatus() == RideStatus.RIDE_COMPLETED).count();
        long cancelledRides = allBookings.stream().filter(b -> b.getStatus() == RideStatus.CANCELLED).count();
        double totalRevenue = paymentService.getTotalRevenue();
        long activeOnlineDrivers = driverRepo.getAllDrivers().stream().filter(Driver::isOnline).count();
        double avgFare = completedRides > 0 ? (allBookings.stream().filter(b -> b.getStatus() == RideStatus.RIDE_COMPLETED).mapToDouble(Booking::getFare).average().orElse(0.0)) : 0.0;

        long ecoCount = allBookings.stream().filter(b -> b.getCabType() == com.errorcab.model.CabType.ECONOMY).count();
        long premCount = allBookings.stream().filter(b -> b.getCabType() == com.errorcab.model.CabType.PREMIUM).count();
        long suvCount = allBookings.stream().filter(b -> b.getCabType() == com.errorcab.model.CabType.SUV).count();

        return ResponseEntity.ok(Map.of(
                "totalPassengers", passengerCount,
                "totalDrivers", driverCount,
                "activeOnlineDrivers", activeOnlineDrivers,
                "totalRides", totalRides,
                "completedRides", completedRides,
                "cancelledRides", cancelledRides,
                "totalRevenue", totalRevenue,
                "averageFare", Math.round(avgFare * 100.0) / 100.0,
                "cabTypeBreakdown", Map.of("ECONOMY", ecoCount, "PREMIUM", premCount, "SUV", suvCount)
        ));
    }

    @GetMapping("/passengers")
    public ResponseEntity<?> getPassengers(HttpServletRequest request) {
        ResponseEntity<?> authCheck = checkAdminAuth(request);
        if (authCheck != null) return authCheck;

        List<Passenger> list = userRepo.getAllUsers().stream()
                .filter(u -> u instanceof Passenger)
                .map(u -> (Passenger) u)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/drivers")
    public ResponseEntity<?> getDrivers(HttpServletRequest request) {
        ResponseEntity<?> authCheck = checkAdminAuth(request);
        if (authCheck != null) return authCheck;

        return ResponseEntity.ok(driverRepo.getAllDrivers());
    }

    @GetMapping("/rides")
    public ResponseEntity<?> getRides(HttpServletRequest request) {
        ResponseEntity<?> authCheck = checkAdminAuth(request);
        if (authCheck != null) return authCheck;

        return ResponseEntity.ok(rideService.getAllBookings());
    }

    @PutMapping("/users/{id}/toggle")
    public ResponseEntity<?> toggleUserActive(@PathVariable int id, @RequestBody Map<String, Boolean> body, HttpServletRequest request) {
        ResponseEntity<?> authCheck = checkAdminAuth(request);
        if (authCheck != null) return authCheck;

        boolean active = body.getOrDefault("active", true);
        boolean ok = userRepo.toggleUserActiveStatus(id, active);
        return ResponseEntity.ok(Map.of("success", ok, "id", id, "active", active));
    }

    @PostMapping("/drivers")
    public ResponseEntity<?> addDriver(@RequestBody Map<String, String> body, HttpServletRequest request) {
        ResponseEntity<?> authCheck = checkAdminAuth(request);
        if (authCheck != null) return authCheck;

        try {
            String name = body.get("name");
            String email = body.get("email");
            String phone = body.get("phone") != null ? body.get("phone") : body.get("phoneNumber");
            String pass = body.get("password");
            if (pass == null || pass.trim().isEmpty()) {
                pass = "password123";
            }
            String confirmPass = body.get("confirmPassword");
            if (confirmPass == null || confirmPass.trim().isEmpty()) {
                confirmPass = pass;
            }

            String license = body.get("licenseNumber") != null ? body.get("licenseNumber") : body.get("license");
            String vehicleModel = body.get("vehicleModel") != null ? body.get("vehicleModel") : "Hyundai Aura";
            String plate = body.get("plateNumber") != null ? body.get("plateNumber") : body.get("licensePlate");
            String cabType = body.get("cabType") != null ? body.get("cabType") : "PREMIUM";
            String color = body.get("color") != null ? body.get("color") : "White";

            Driver d = authService.registerDriver(
                    name,
                    email,
                    phone,
                    pass,
                    confirmPass,
                    license,
                    vehicleModel,
                    plate,
                    cabType,
                    color
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Driver successfully added to ERRORCab fleet.",
                    "driver", d
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Failed to register driver."));
        }
    }
}
