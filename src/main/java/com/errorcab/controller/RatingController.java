package com.errorcab.controller;

import com.errorcab.model.Booking;
import com.errorcab.model.Rating;
import com.errorcab.service.BookingService;
import com.errorcab.service.RatingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Passenger Ratings & Driver Performance Reviews.
 */
@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    private final RatingService ratingService = RatingService.getInstance();
    private final BookingService bookingService;

    public RatingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<?> submitRating(@RequestBody Map<String, Object> body) {
        try {
            int bookingId = Integer.parseInt(body.get("bookingId").toString());
            int stars = Integer.parseInt(body.get("stars").toString());
            String review = body.get("review") != null ? body.get("review").toString() : 
                           (body.get("comment") != null ? body.get("comment").toString() : "");

            Optional<Booking> bOpt = bookingService.getBooking(bookingId);
            if (bOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not found."));
            }

            Rating rating = ratingService.submitRating(bOpt.get(), stars, review);
            return ResponseEntity.ok(rating);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<?> getRating(@PathVariable int bookingId) {
        return ratingService.getRatingForBooking(bookingId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/check/{bookingId}")
    public ResponseEntity<?> checkRated(@PathVariable int bookingId) {
        boolean rated = ratingService.hasRated(bookingId);
        return ResponseEntity.ok(Map.of("hasRated", rated));
    }
}
