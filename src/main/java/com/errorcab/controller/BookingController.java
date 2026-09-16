package com.errorcab.controller;

import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.Passenger;
import com.errorcab.model.RideStatus;
import com.errorcab.model.User;
import com.errorcab.service.AuthenticationService;
import com.errorcab.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Cab Bookings, Fare Estimation, and Ride Lifecycle Progression.
 */
@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService bookingService;
    private final AuthenticationService authService;

    public BookingController(BookingService bookingService, AuthenticationService authService) {
        this.bookingService = bookingService;
        this.authService = authService;
    }

    @GetMapping("/estimate")
    public ResponseEntity<?> getEstimate(
            @RequestParam String pickup,
            @RequestParam String destination,
            @RequestParam(defaultValue = "ECONOMY") String cabType,
            @RequestParam(required = false) String promoCode) {
        try {
            CabType type = CabType.valueOf(cabType.toUpperCase().trim());
            var est = bookingService.estimateFare(pickup, destination, type, promoCode);
            return ResponseEntity.ok(est);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createBooking(@RequestBody Map<String, Object> body) {
        try {
            int passengerId = Integer.parseInt(body.get("passengerId").toString());
            String pickup = body.containsKey("pickup") && body.get("pickup") != null ? body.get("pickup").toString() :
                           (body.get("pickupLocation") != null ? body.get("pickupLocation").toString() : "");
            String destination = body.containsKey("destination") && body.get("destination") != null ? body.get("destination").toString() :
                                (body.containsKey("dropoff") && body.get("dropoff") != null ? body.get("dropoff").toString() :
                                (body.get("dropoffLocation") != null ? body.get("dropoffLocation").toString() : ""));
            String cabTypeStr = body.get("cabType").toString();
            String promoCode = body.get("promoCode") != null ? body.get("promoCode").toString() : null;

            Optional<User> userOpt = authService.getUserById(passengerId);
            if (userOpt.isEmpty() || !(userOpt.get() instanceof Passenger)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid passenger account."));
            }

            Passenger passenger = (Passenger) userOpt.get();
            CabType cabType = CabType.valueOf(cabTypeStr.toUpperCase().trim());

            Booking booking = bookingService.bookRide(passenger, pickup, destination, cabType, promoCode);
            return ResponseEntity.ok(booking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBooking(@PathVariable int id) {
        return bookingService.getBooking(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/active/passenger/{passengerId}")
    public ResponseEntity<?> getActivePassengerBooking(@PathVariable int passengerId) {
        var opt = bookingService.getActiveBookingForPassenger(passengerId);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        }
        return ResponseEntity.ok(Map.of("active", false));
    }

    @GetMapping("/active/driver/{driverId}")
    public ResponseEntity<?> getActiveDriverBooking(@PathVariable int driverId) {
        var opt = bookingService.getActiveBookingForDriver(driverId);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        }
        return ResponseEntity.ok(Map.of("active", false));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<?> getPassengerHistory(@PathVariable int passengerId) {
        return ResponseEntity.ok(bookingService.getPassengerHistory(passengerId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<?> getDriverHistory(@PathVariable int driverId) {
        return ResponseEntity.ok(bookingService.getDriverHistory(driverId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        try {
            RideStatus target = RideStatus.valueOf(body.get("status").toUpperCase().trim());
            boolean ok = bookingService.advanceStatus(id, target);
            return ResponseEntity.ok(Map.of("success", ok, "status", target.name()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable int id, @RequestBody(required = false) Map<String, String> body) {
        try {
            String reason = body != null && body.containsKey("reason") ? body.get("reason") : "Changed plans";
            boolean ok = bookingService.cancelRide(id, reason);
            return ResponseEntity.ok(Map.of("success", ok, "status", "CANCELLED", "reason", reason));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
