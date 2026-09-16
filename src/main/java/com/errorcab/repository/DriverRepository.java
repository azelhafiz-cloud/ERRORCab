package com.errorcab.repository;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.CabType;
import com.errorcab.model.Driver;
import com.errorcab.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access repository for Drivers and Vehicles.
 */
public class DriverRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<Driver> findByUserId(int userId) {
        String sql = "SELECT u.id, u.name, u.email, u.phone, u.password, u.active, " +
                "d.license_number, d.vehicle_id, d.is_online, d.rating, d.rating_count, " +
                "d.total_earnings, d.completed_rides_count, d.current_location, " +
                "v.model, v.plate_number, v.cab_type, v.color " +
                "FROM users u " +
                "JOIN drivers d ON u.id = d.user_id " +
                "JOIN vehicles v ON d.vehicle_id = v.id " +
                "WHERE u.id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapDriver(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Driver> findAvailableDrivers(CabType cabType) {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.phone, u.password, u.active, " +
                "d.license_number, d.vehicle_id, d.is_online, d.rating, d.rating_count, " +
                "d.total_earnings, d.completed_rides_count, d.current_location, " +
                "v.model, v.plate_number, v.cab_type, v.color " +
                "FROM users u " +
                "JOIN drivers d ON u.id = d.user_id " +
                "JOIN vehicles v ON d.vehicle_id = v.id " +
                "WHERE u.active = 1 AND d.is_online = 1 AND v.cab_type = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cabType.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    drivers.add(mapDriver(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return drivers;
    }

    public List<Driver> getAllDrivers() {
        List<Driver> drivers = new ArrayList<>();
        String sql = "SELECT u.id, u.name, u.email, u.phone, u.password, u.active, " +
                "d.license_number, d.vehicle_id, d.is_online, d.rating, d.rating_count, " +
                "d.total_earnings, d.completed_rides_count, d.current_location, " +
                "v.model, v.plate_number, v.cab_type, v.color " +
                "FROM users u " +
                "JOIN drivers d ON u.id = d.user_id " +
                "JOIN vehicles v ON d.vehicle_id = v.id " +
                "ORDER BY u.id ASC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                drivers.add(mapDriver(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return drivers;
    }

    public boolean updateOnlineStatus(int userId, boolean online) {
        String sql = "UPDATE drivers SET is_online = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, online ? 1 : 0);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateRating(int userId, double newRating, int newCount) {
        String sql = "UPDATE drivers SET rating = ?, rating_count = ? WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newRating);
            ps.setInt(2, newCount);
            ps.setInt(3, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordCompletedTrip(int userId, double fare) {
        String sql = "UPDATE drivers SET total_earnings = total_earnings + ?, completed_rides_count = completed_rides_count + 1 WHERE user_id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, fare);
            ps.setInt(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Driver mapDriver(ResultSet rs) throws SQLException {
        Driver driver = new Driver(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("password"),
                rs.getInt("active") == 1,
                rs.getString("license_number"),
                rs.getInt("vehicle_id"),
                rs.getInt("is_online") == 1,
                rs.getDouble("rating"),
                rs.getInt("rating_count"),
                rs.getDouble("total_earnings"),
                rs.getInt("completed_rides_count"),
                rs.getString("current_location")
        );
        Vehicle v = new Vehicle(
                rs.getInt("vehicle_id"),
                rs.getString("model"),
                rs.getString("plate_number"),
                CabType.valueOf(rs.getString("cab_type")),
                rs.getString("color")
        );
        driver.setVehicle(v);
        return driver;
    }
}
