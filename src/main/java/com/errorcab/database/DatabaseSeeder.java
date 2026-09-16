package com.errorcab.database;

import com.errorcab.config.AppConfig;
import com.errorcab.model.CabType;
import com.errorcab.model.PaymentMethod;
import com.errorcab.model.PaymentStatus;
import com.errorcab.model.RideStatus;
import com.errorcab.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

/**
 * Seeds initial demo data for Indian passengers, drivers, vehicles, and past bookings.
 * Ensures the application is immediately rich and ready for OOP viva demonstrations.
 */
public class DatabaseSeeder {

    public static void seedIfEmpty() {
        DatabaseManager db = DatabaseManager.getInstance();
        try (Connection conn = db.getConnection()) {
            if (isDatabaseSeeded(conn)) {
                upgradeExistingPlainTextPasswords(conn);
                seedPromoCodesAndFavoritesIfMissing();
                return;
            }

            System.out.println("Seeding demo data for ERRORCab...");

            String demoHash = PasswordUtil.hash(AppConfig.DEMO_PASSWORD);

            // 1. Seed Vehicles
            int vDzire = insertVehicle(conn, "Maruti Suzuki Dzire", "KL 07 AB 1234", CabType.ECONOMY.name(), "Arctic White");
            int vAura = insertVehicle(conn, "Hyundai Aura", "KL 39 C 4567", CabType.PREMIUM.name(), "Titan Grey");
            int vErtiga = insertVehicle(conn, "Maruti Suzuki Ertiga", "KL 01 AX 7821", CabType.SUV.name(), "Magma Grey");
            int vEtios = insertVehicle(conn, "Toyota Etios", "KL 41 D 9087", CabType.ECONOMY.name(), "Silver Metallic");

            // 2. Seed Admin
            insertUser(conn, "Admin Operations", AppConfig.DEMO_ADMIN_EMAIL, "+91 98470 00000", demoHash, "ADMIN", 1);

            // 3. Seed Passengers
            int pRahul = insertUser(conn, "Rahul Nair", AppConfig.DEMO_PASSENGER_EMAIL, "+91 98471 23456", demoHash, "PASSENGER", 1);
            insertPassenger(conn, pRahul, "Kakkanad, Kochi", 14, 2850.0);

            int pAnjali = insertUser(conn, "Anjali Menon", "anjali@example.com", "+91 98472 34567", demoHash, "PASSENGER", 1);
            insertPassenger(conn, pAnjali, "Edappally, Kochi", 8, 1620.0);

            int pSneha = insertUser(conn, "Sneha Thomas", "sneha@example.com", "+91 98473 45678", demoHash, "PASSENGER", 1);
            insertPassenger(conn, pSneha, "Vyttila, Kochi", 5, 980.0);

            // 4. Seed Drivers
            int dAkhil = insertUser(conn, "Akhil Raj", AppConfig.DEMO_DRIVER_EMAIL, "+91 94471 12345", demoHash, "DRIVER", 1);
            insertDriver(conn, dAkhil, "KL-07-2018-0098765", vDzire, 1, 4.9, 48, 14250.0, 52, "Kakkanad");

            int dVishnu = insertUser(conn, "Vishnu Nair", "vishnu@example.com", "+91 94472 23456", demoHash, "DRIVER", 1);
            insertDriver(conn, dVishnu, "KL-39-2019-0012345", vAura, 1, 4.8, 36, 12800.0, 41, "Edappally");

            int dManu = insertUser(conn, "Manu Joseph", "manu@example.com", "+91 94473 34567", demoHash, "DRIVER", 1);
            insertDriver(conn, dManu, "KL-01-2017-0054321", vErtiga, 1, 4.9, 58, 22400.0, 60, "Vyttila");

            int dArjun = insertUser(conn, "Arjun Kumar", "arjun@example.com", "+91 94474 45678", demoHash, "DRIVER", 1);
            insertDriver(conn, dArjun, "KL-41-2020-0078912", vEtios, 1, 4.7, 24, 8900.0, 30, "Palarivattom");

            // 5. Seed Past Completed Bookings with Payments and Ratings
            int b1 = insertBooking(conn, "EC-1024", pRahul, "Rahul Nair", "+91 98471 23456",
                    dAkhil, "Akhil Raj", "+91 94471 12345", "Maruti Suzuki Dzire", "KL 07 AB 1234", 4.9,
                    "Kakkanad", "Vyttila", 9.2, 24, CabType.ECONOMY.name(), 179.0, RideStatus.RIDE_COMPLETED.name(),
                    null, LocalDateTime.now().minusDays(2).toString(), LocalDateTime.now().minusDays(2).plusMinutes(35).toString());
            insertPayment(conn, b1, 179.0, PaymentMethod.UPI.name(), PaymentStatus.PAID.name(), "UPI-7819203912");
            insertRating(conn, b1, dAkhil, pRahul, 5, "Driver was polite and arrived on time.");

            int b2 = insertBooking(conn, "EC-1018", pRahul, "Rahul Nair", "+91 98471 23456",
                    dVishnu, "Vishnu Nair", "+91 94472 23456", "Hyundai Aura", "KL 39 C 4567", 4.8,
                    "Edappally", "Fort Kochi", 16.5, 42, CabType.PREMIUM.name(), 410.0, RideStatus.RIDE_COMPLETED.name(),
                    null, LocalDateTime.now().minusDays(5).toString(), LocalDateTime.now().minusDays(5).plusMinutes(50).toString());
            insertPayment(conn, b2, 410.0, PaymentMethod.CARD.name(), PaymentStatus.PAID.name(), "CARD-918230192");
            insertRating(conn, b2, dVishnu, pRahul, 5, "Very clean premium sedan, smooth AC ride.");

            int b3 = insertBooking(conn, "EC-1005", pAnjali, "Anjali Menon", "+91 98472 34567",
                    dManu, "Manu Joseph", "+91 94473 34567", "Maruti Suzuki Ertiga", "KL 01 AX 7821", 4.9,
                    "Aluva", "MG Road", 20.0, 48, CabType.SUV.name(), 600.0, RideStatus.RIDE_COMPLETED.name(),
                    null, LocalDateTime.now().minusDays(7).toString(), LocalDateTime.now().minusDays(7).plusMinutes(55).toString());
            insertPayment(conn, b3, 600.0, PaymentMethod.UPI.name(), PaymentStatus.PAID.name(), "UPI-4819204910");
            insertRating(conn, b3, dManu, pAnjali, 5, "Spacious cab, ideal for family airport transit.");

            // 6. Seed sample notifications
            insertNotification(conn, pRahul, "Welcome to ERRORCab!", "Book smart, ride safe with Kochi's premier cab service.");
            insertNotification(conn, dAkhil, "Welcome Partner!", "Keep your status ONLINE to receive real-time ride requests.");

            // 7. Seed Favorites
            insertFavorite(conn, pRahul, "Home", "Kakkanad", "Infopark Campus, Kakkanad");
            insertFavorite(conn, pRahul, "Work", "Edappally", "Lulu Cyber Park, Edappally");

            // 8. Seed Promo Codes
            insertPromo(conn, "WELCOME", 50.0, 100.0, "Welcome offer: Flat ₹50 off your ride");
            insertPromo(conn, "ERROR50", 50.0, 120.0, "Special Team ERROR ₹50 discount voucher");
            insertPromo(conn, "FIRST50", 50.0, 100.0, "First ride special ₹50 off across Kerala");

            System.out.println("ERRORCab demo data seeded successfully.");
        } catch (SQLException e) {
            System.err.println("Seeding error: " + e.getMessage());
            e.printStackTrace();
        }

        // Ensure promo codes and favorites exist even if user table was already populated
        seedPromoCodesAndFavoritesIfMissing();
    }

    private static void seedPromoCodesAndFavoritesIfMissing() {
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            insertPromo(conn, "WELCOME", 50.0, 100.0, "Welcome offer: Flat ₹50 off your ride");
            insertPromo(conn, "ERROR50", 50.0, 120.0, "Special Team ERROR ₹50 discount voucher");
            insertPromo(conn, "FIRST50", 50.0, 100.0, "First ride special ₹50 off across Kerala");
        } catch (Exception ignored) {}
    }

    private static void insertFavorite(Connection conn, int pId, String label, String loc, String address) {
        String sql = "INSERT INTO favorites (passenger_id, label, location_name, address, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pId);
            ps.setString(2, label);
            ps.setString(3, loc);
            ps.setString(4, address);
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private static void insertPromo(Connection conn, String code, double discount, double minFare, String desc) {
        String sql = "INSERT OR IGNORE INTO promo_codes (code, discount_amount, minimum_fare, description, active) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            ps.setDouble(2, discount);
            ps.setDouble(3, minFare);
            ps.setString(4, desc);
            ps.executeUpdate();
        } catch (SQLException ignored) {}
    }

    private static void upgradeExistingPlainTextPasswords(Connection conn) {
        try {
            String sql = "SELECT id, password FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                java.util.Map<Integer, String> toUpdate = new java.util.HashMap<>();
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String pass = rs.getString("password");
                    // If stored password is not a 64-char hex SHA-256 string, upgrade it
                    if (pass != null && pass.length() < 64) {
                        toUpdate.put(id, PasswordUtil.hash(pass));
                    }
                }
                if (!toUpdate.isEmpty()) {
                    String updateSql = "UPDATE users SET password = ? WHERE id = ?";
                    try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                        for (var entry : toUpdate.entrySet()) {
                            ps.setString(1, entry.getValue());
                            ps.setInt(2, entry.getKey());
                            ps.executeUpdate();
                        }
                    }
                    System.out.println("Upgraded " + toUpdate.size() + " legacy plain-text user passwords to secure SHA-256 hashes.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Could not run password hash migration: " + e.getMessage());
        }
    }

    private static boolean isDatabaseSeeded(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static int insertVehicle(Connection conn, String model, String plate, String type, String color) throws SQLException {
        String sql = "INSERT INTO vehicles (model, plate_number, cab_type, color) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, model);
            ps.setString(2, plate);
            ps.setString(3, type);
            ps.setString(4, color);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static int insertUser(Connection conn, String name, String email, String phone, String password, String role, int active) throws SQLException {
        String sql = "INSERT INTO users (name, email, phone, password, role, active, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setString(4, password);
            ps.setString(5, role);
            ps.setInt(6, active);
            ps.setString(7, LocalDateTime.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static void insertPassenger(Connection conn, int userId, String address, int totalRides, double spent) throws SQLException {
        String sql = "INSERT INTO passengers (user_id, default_address, total_rides, total_spent) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, address);
            ps.setInt(3, totalRides);
            ps.setDouble(4, spent);
            ps.executeUpdate();
        }
    }

    private static void insertDriver(Connection conn, int userId, String license, int vehicleId, int online,
                                     double rating, int ratingCount, double earnings, int ridesCount, String loc) throws SQLException {
        String sql = "INSERT INTO drivers (user_id, license_number, vehicle_id, is_online, rating, rating_count, total_earnings, completed_rides_count, current_location) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, license);
            ps.setInt(3, vehicleId);
            ps.setInt(4, online);
            ps.setDouble(5, rating);
            ps.setInt(6, ratingCount);
            ps.setDouble(7, earnings);
            ps.setInt(8, ridesCount);
            ps.setString(9, loc);
            ps.executeUpdate();
        }
    }

    private static int insertBooking(Connection conn, String code, int pId, String pName, String pPhone,
                                     Integer dId, String dName, String dPhone, String vModel, String vPlate,
                                     double dRating, String pickup, String dest, double dist, int mins,
                                     String cabType, double fare, String status, String reason,
                                     String created, String completed) throws SQLException {
        String sql = "INSERT INTO bookings (booking_code, passenger_id, passenger_name, passenger_phone, driver_id, " +
                "driver_name, driver_phone, vehicle_model, vehicle_plate_number, driver_rating, pickup_location, " +
                "destination_location, distance_km, estimated_minutes, cab_type, fare, status, cancellation_reason, created_at, completed_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setInt(2, pId);
            ps.setString(3, pName);
            ps.setString(4, pPhone);
            if (dId != null) ps.setInt(5, dId); else ps.setNull(5, java.sql.Types.INTEGER);
            ps.setString(6, dName);
            ps.setString(7, dPhone);
            ps.setString(8, vModel);
            ps.setString(9, vPlate);
            ps.setDouble(10, dRating);
            ps.setString(11, pickup);
            ps.setString(12, dest);
            ps.setDouble(13, dist);
            ps.setInt(14, mins);
            ps.setString(15, cabType);
            ps.setDouble(16, fare);
            ps.setString(17, status);
            ps.setString(18, reason);
            ps.setString(19, created);
            ps.setString(20, completed);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private static void insertPayment(Connection conn, int bookingId, double amount, String method, String status, String ref) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, method, status, transaction_ref, paid_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setDouble(2, amount);
            ps.setString(3, method);
            ps.setString(4, status);
            ps.setString(5, ref);
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private static void insertRating(Connection conn, int bookingId, int driverId, int passengerId, int stars, String review) throws SQLException {
        String sql = "INSERT INTO ratings (booking_id, driver_id, passenger_id, stars, review, created_at) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            ps.setInt(2, driverId);
            ps.setInt(3, passengerId);
            ps.setInt(4, stars);
            ps.setString(5, review);
            ps.setString(6, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }

    private static void insertNotification(Connection conn, int userId, String title, String msg) throws SQLException {
        String sql = "INSERT INTO notifications (user_id, title, message, timestamp, is_read) VALUES (?, ?, ?, ?, 0)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, title);
            ps.setString(3, msg);
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
        }
    }
}
