package com.errorcab.repository;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.Rating;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository for managing passenger ratings and updating driver scores.
 */
public class RatingRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public boolean hasPassengerRated(int bookingId) {
        String sql = "SELECT 1 FROM ratings WHERE booking_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Rating submitRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (booking_id, driver_id, passenger_id, stars, review, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, rating.getBookingId());
                    ps.setInt(2, rating.getDriverId());
                    ps.setInt(3, rating.getPassengerId());
                    ps.setInt(4, rating.getStars());
                    ps.setString(5, rating.getReview());
                    ps.setString(6, rating.getCreatedAt().toString());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            rating.setId(rs.getInt(1));
                        }
                    }
                }

                // Recalculate driver's aggregate rating
                String avgSql = "SELECT AVG(stars), COUNT(stars) FROM ratings WHERE driver_id = ?";
                double newAvg = 5.0;
                int count = 1;
                try (PreparedStatement avgPs = conn.prepareStatement(avgSql)) {
                    avgPs.setInt(1, rating.getDriverId());
                    try (ResultSet ars = avgPs.executeQuery()) {
                        if (ars.next()) {
                            newAvg = Math.round(ars.getDouble(1) * 10.0) / 10.0;
                            count = ars.getInt(2);
                        }
                    }
                }

                String updateDriverSql = "UPDATE drivers SET rating = ?, rating_count = ? WHERE user_id = ?";
                try (PreparedStatement upPs = conn.prepareStatement(updateDriverSql)) {
                    upPs.setDouble(1, newAvg);
                    upPs.setInt(2, count);
                    upPs.setInt(3, rating.getDriverId());
                    upPs.executeUpdate();
                }

                conn.commit();
                return rating;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Optional<Rating> getRatingForBooking(int bookingId) {
        String sql = "SELECT * FROM ratings WHERE booking_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Rating(
                            rs.getInt("id"),
                            rs.getInt("booking_id"),
                            rs.getInt("driver_id"),
                            rs.getInt("passenger_id"),
                            rs.getInt("stars"),
                            rs.getString("review"),
                            LocalDateTime.parse(rs.getString("created_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}
