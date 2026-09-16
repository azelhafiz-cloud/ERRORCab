package com.errorcab.repository;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.Admin;
import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.Role;
import com.errorcab.model.User;
import com.errorcab.model.Vehicle;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data access repository for User entities.
 * Demonstrates Polymorphic hydration of Passenger, Driver, and Admin subclasses.
 */
public class UserRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapUser(conn, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapUser(conn, rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Passenger registerPassenger(String name, String email, String phone, String password, String address) throws SQLException {
        String userSql = "INSERT INTO users (name, email, phone, password, role, active, created_at) VALUES (?, ?, ?, ?, 'PASSENGER', 1, ?)";
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, email.toLowerCase().trim());
                    ps.setString(3, phone);
                    ps.setString(4, password);
                    ps.setString(5, LocalDateTime.now().toString());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Failed to retrieve generated user ID");
                        userId = rs.getInt(1);
                    }
                }

                String passSql = "INSERT INTO passengers (user_id, default_address, total_rides, total_spent) VALUES (?, ?, 0, 0.0)";
                try (PreparedStatement ps = conn.prepareStatement(passSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, address != null ? address : "Kochi");
                    ps.executeUpdate();
                }

                conn.commit();
                return new Passenger(userId, name, email, phone, password, true, address, 0, 0.0);
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Driver registerDriver(String name, String email, String phone, String password,
                                 String license, String vehicleModel, String plateNumber,
                                 String cabType, String color) throws SQLException {
        try (Connection conn = db.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert vehicle
                int vehicleId;
                String vSql = "INSERT INTO vehicles (model, plate_number, cab_type, color) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(vSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, vehicleModel);
                    ps.setString(2, plateNumber.toUpperCase().trim());
                    ps.setString(3, cabType);
                    ps.setString(4, color != null ? color : "White");
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Failed to create vehicle");
                        vehicleId = rs.getInt(1);
                    }
                }

                // 2. Insert user
                int userId;
                String uSql = "INSERT INTO users (name, email, phone, password, role, active, created_at) VALUES (?, ?, ?, ?, 'DRIVER', 1, ?)";
                try (PreparedStatement ps = conn.prepareStatement(uSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, email.toLowerCase().trim());
                    ps.setString(3, phone);
                    ps.setString(4, password);
                    ps.setString(5, LocalDateTime.now().toString());
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (!rs.next()) throw new SQLException("Failed to create driver user");
                        userId = rs.getInt(1);
                    }
                }

                // 3. Insert driver info
                String dSql = "INSERT INTO drivers (user_id, license_number, vehicle_id, is_online, rating, rating_count, total_earnings, completed_rides_count, current_location) " +
                        "VALUES (?, ?, ?, 1, 5.0, 1, 0.0, 0, 'Kakkanad')";
                try (PreparedStatement ps = conn.prepareStatement(dSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, license.toUpperCase().trim());
                    ps.setInt(3, vehicleId);
                    ps.executeUpdate();
                }

                conn.commit();
                Driver driver = new Driver(userId, name, email, phone, password, true, license, vehicleId, true, 5.0, 1, 0.0, 0, "Kakkanad");
                driver.setVehicle(new Vehicle(vehicleId, vehicleModel, plateNumber, com.errorcab.model.CabType.valueOf(cabType), color));
                return driver;
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id ASC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User u = mapUser(conn, rs);
                if (u != null) list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean toggleUserActiveStatus(int userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, active ? 1 : 0);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapUser(Connection conn, ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String password = rs.getString("password");
        Role role = Role.valueOf(rs.getString("role"));
        boolean active = rs.getInt("active") == 1;

        if (role == Role.PASSENGER) {
            String pSql = "SELECT * FROM passengers WHERE user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(pSql)) {
                ps.setInt(1, id);
                try (ResultSet prs = ps.executeQuery()) {
                    if (prs.next()) {
                        return new Passenger(id, name, email, phone, password, active,
                                prs.getString("default_address"),
                                prs.getInt("total_rides"),
                                prs.getDouble("total_spent"));
                    }
                }
            }
            return new Passenger(id, name, email, phone, password, active, "Kochi", 0, 0.0);
        } else if (role == Role.DRIVER) {
            String dSql = "SELECT d.*, v.model, v.plate_number, v.cab_type, v.color " +
                    "FROM drivers d " +
                    "LEFT JOIN vehicles v ON d.vehicle_id = v.id " +
                    "WHERE d.user_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(dSql)) {
                ps.setInt(1, id);
                try (ResultSet drs = ps.executeQuery()) {
                    if (drs.next()) {
                        Driver driver = new Driver(id, name, email, phone, password, active,
                                drs.getString("license_number"),
                                drs.getInt("vehicle_id"),
                                drs.getInt("is_online") == 1,
                                drs.getDouble("rating"),
                                drs.getInt("rating_count"),
                                drs.getDouble("total_earnings"),
                                drs.getInt("completed_rides_count"),
                                drs.getString("current_location"));
                        if (drs.getString("model") != null) {
                            Vehicle v = new Vehicle(drs.getInt("vehicle_id"),
                                    drs.getString("model"),
                                    drs.getString("plate_number"),
                                    com.errorcab.model.CabType.valueOf(drs.getString("cab_type")),
                                    drs.getString("color"));
                            driver.setVehicle(v);
                        }
                        return driver;
                    }
                }
            }
            return new Driver(name, email, phone, password, "KL-07-NEW");
        } else {
            return new Admin(id, name, email, phone, password, active, "Operations & Dispatch");
        }
    }
}
