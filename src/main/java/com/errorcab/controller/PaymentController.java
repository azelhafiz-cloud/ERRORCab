package com.errorcab.controller;

import com.errorcab.model.Booking;
import com.errorcab.model.Payment;
import com.errorcab.model.PaymentMethod;
import com.errorcab.service.BookingService;
import com.errorcab.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Demo Payments (Cash, UPI, Card).
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService = PaymentService.getInstance();
    private final BookingService bookingService;

    public PaymentController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping({"", "/process"})
    public ResponseEntity<?> processPayment(@RequestBody Map<String, Object> body) {
        try {
            int bookingId = Integer.parseInt(body.get("bookingId").toString());
            String methodStr = body.containsKey("method") && body.get("method") != null ? body.get("method").toString() : "UPI";
            PaymentMethod method = PaymentMethod.valueOf(methodStr.toUpperCase().trim());

            Optional<Booking> bookingOpt = bookingService.getBooking(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Booking not found."));
            }

            Payment payment = paymentService.processPayment(bookingOpt.get(), method);
            return ResponseEntity.ok(payment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping({"/{bookingId}", "/booking/{bookingId}"})
    public ResponseEntity<?> getPaymentForBooking(@PathVariable int bookingId) {
        return paymentService.getPaymentForBooking(bookingId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
