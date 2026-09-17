package com.errorcab.service;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.Booking;
import com.errorcab.model.Payment;
import com.errorcab.model.PaymentMethod;
import com.errorcab.model.PaymentStatus;
import com.errorcab.repository.PaymentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service managing demo payments (Cash, UPI, Card).
 * Completely local and offline simulation without external gateway dependencies.
 */
public class PaymentService {
    private static PaymentService instance;
    private final PaymentRepository paymentRepo = new PaymentRepository();
    private final DatabaseManager db = DatabaseManager.getInstance();

    private PaymentService() {}

    public static synchronized PaymentService getInstance() {
        if (instance == null) {
            instance = new PaymentService();
        }
        return instance;
    }

    public Payment processPayment(Booking booking, PaymentMethod method) throws SQLException {
        String txnRef = method.name() + "-" + (System.currentTimeMillis() % 10000000);
        Payment payment = new Payment(
                0,
                booking.getId(),
                booking.getFare(),
                method,
                PaymentStatus.PAID,
                txnRef,
                LocalDateTime.now()
        );

        Payment saved = paymentRepo.recordPayment(payment);

        // Update passenger's total spent
        String sql = "UPDATE passengers SET total_spent = total_spent + ?, total_rides = total_rides + 1 WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, booking.getFare());
            ps.setInt(2, booking.getPassengerId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        NotificationService.getInstance().sendNotification(booking.getPassengerId(), "Payment Successful! 💳",
                "Payment of ₹" + (int) booking.getFare() + " via " + method.getDisplayName() + " confirmed. Ref: " + txnRef);

        return saved;
    }

    public Optional<Payment> getPaymentForBooking(int bookingId) {
        return paymentRepo.findByBookingId(bookingId);
    }

    public double getTotalRevenue() {
        return paymentRepo.getTotalRevenue();
    }

    public java.util.List<java.util.Map<String, Object>> getPaymentsByUser(int userId) {
        return paymentRepo.findPaymentsByUser(userId);
    }

    public java.util.List<java.util.Map<String, Object>> getAllPayments() {
        return paymentRepo.findAllPayments();
    }
}
