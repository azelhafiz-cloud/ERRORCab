package com.errorcab.controller;

import com.errorcab.model.Booking;
import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.RideStatus;
import com.errorcab.model.Role;
import com.errorcab.model.User;
import com.errorcab.repository.DriverRepository;
import com.errorcab.repository.UserRepository;
import com.errorcab.service.PaymentService;
import com.errorcab.service.RideService;
import com.errorcab.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    public AdminController(SessionService sessionService) {
        this.sessionService = sessionService;
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

        return ResponseEntity.ok(Map.of(
                "totalPassengers", passengerCount,
                "totalDrivers", driverCount,
                "totalRides", totalRides,
                "completedRides", completedRides,
                "cancelledRides", cancelledRides,
                "totalRevenue", totalRevenue
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
}
