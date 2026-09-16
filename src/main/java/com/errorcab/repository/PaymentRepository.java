package com.errorcab.repository;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.Payment;
import com.errorcab.model.PaymentMethod;
import com.errorcab.model.PaymentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for handling payment records and revenue aggregation.
 */
public class PaymentRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Payment recordPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, method, status, transaction_ref, paid_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getBookingId());
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getMethod().name());
            ps.setString(4, payment.getStatus().name());
            ps.setString(5, payment.getTransactionRef());
            ps.setString(6, payment.getPaidAt().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    payment.setId(rs.getInt(1));
                }
            }
        }
        return payment;
    }

    public Optional<Payment> findByBookingId(int bookingId) {
        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Payment(
                            rs.getInt("id"),
                            rs.getInt("booking_id"),
                            rs.getDouble("amount"),
                            PaymentMethod.valueOf(rs.getString("method")),
                            PaymentStatus.valueOf(rs.getString("status")),
                            rs.getString("transaction_ref"),
                            LocalDateTime.parse(rs.getString("paid_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public double getTotalRevenue() {
        String sql = "SELECT SUM(amount) FROM payments WHERE status = 'PAID'";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}
