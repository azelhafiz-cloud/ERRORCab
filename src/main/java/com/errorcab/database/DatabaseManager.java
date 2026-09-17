package com.errorcab.database;

import com.errorcab.config.AppConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite JDBC connection and database schema initialization.
 * Follows the Singleton pattern for connection management.
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
        initDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(AppConfig.DB_URL);
        // Enable foreign key constraints in SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    private void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT UNIQUE NOT NULL,
                    phone TEXT NOT NULL,
                    password TEXT NOT NULL,
                    role TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1,
                    created_at TEXT NOT NULL
                );
            """);

            // 2. Vehicles Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS vehicles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    model TEXT NOT NULL,
                    plate_number TEXT UNIQUE NOT NULL,
                    cab_type TEXT NOT NULL,
                    color TEXT NOT NULL
                );
            """);

            // 3. Drivers Table (extends user info)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS drivers (
                    user_id INTEGER PRIMARY KEY,
                    license_number TEXT UNIQUE NOT NULL,
                    vehicle_id INTEGER NOT NULL,
                    is_online INTEGER NOT NULL DEFAULT 1,
                    rating REAL NOT NULL DEFAULT 5.0,
                    rating_count INTEGER NOT NULL DEFAULT 1,
                    total_earnings REAL NOT NULL DEFAULT 0.0,
                    completed_rides_count INTEGER NOT NULL DEFAULT 0,
                    current_location TEXT NOT NULL DEFAULT 'Kakkanad',
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE
                );
            """);

            // 4. Passengers Table (extends user info)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS passengers (
                    user_id INTEGER PRIMARY KEY,
                    default_address TEXT,
                    total_rides INTEGER NOT NULL DEFAULT 0,
                    total_spent REAL NOT NULL DEFAULT 0.0,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // 5. Bookings Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bookings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_code TEXT UNIQUE NOT NULL,
                    passenger_id INTEGER NOT NULL,
                    passenger_name TEXT NOT NULL,
                    passenger_phone TEXT NOT NULL,
                    driver_id INTEGER,
                    driver_name TEXT,
                    driver_phone TEXT,
                    vehicle_model TEXT,
                    vehicle_plate_number TEXT,
                    driver_rating REAL DEFAULT 5.0,
                    pickup_location TEXT NOT NULL,
                    destination_location TEXT NOT NULL,
                    distance_km REAL NOT NULL,
                    estimated_minutes INTEGER NOT NULL,
                    cab_type TEXT NOT NULL,
                    fare REAL NOT NULL,
                    status TEXT NOT NULL,
                    cancellation_reason TEXT,
                    created_at TEXT NOT NULL,
                    completed_at TEXT,
                    FOREIGN KEY (passenger_id) REFERENCES users(id),
                    FOREIGN KEY (driver_id) REFERENCES users(id)
                );
            """);

            // 6. Payments Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_id INTEGER NOT NULL,
                    amount REAL NOT NULL,
                    method TEXT NOT NULL,
                    status TEXT NOT NULL,
                    transaction_ref TEXT UNIQUE NOT NULL,
                    paid_at TEXT NOT NULL,
                    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
                );
            """);

            // 7. Ratings Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ratings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_id INTEGER UNIQUE NOT NULL,
                    driver_id INTEGER NOT NULL,
                    passenger_id INTEGER NOT NULL,
                    stars INTEGER NOT NULL,
                    review TEXT,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
                    FOREIGN KEY (driver_id) REFERENCES users(id),
                    FOREIGN KEY (passenger_id) REFERENCES users(id)
                );
            """);

            // 8. Notifications Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL,
                    title TEXT NOT NULL,
                    message TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    is_read INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // 9. Favorites Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    passenger_id INTEGER NOT NULL,
                    label TEXT NOT NULL,
                    location_name TEXT NOT NULL,
                    address TEXT,
                    created_at TEXT NOT NULL,
                    FOREIGN KEY (passenger_id) REFERENCES users(id) ON DELETE CASCADE
                );
            """);

            // 10. Promo Codes Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS promo_codes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    code TEXT UNIQUE NOT NULL,
                    discount_amount REAL NOT NULL,
                    minimum_fare REAL NOT NULL,
                    description TEXT,
                    active INTEGER NOT NULL DEFAULT 1
                );
            """);

            // Gracefully add promo_code and discount columns to bookings if they don't exist
            try {
                stmt.execute("ALTER TABLE bookings ADD COLUMN promo_code TEXT;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE bookings ADD COLUMN discount REAL DEFAULT 0.0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE bookings ADD COLUMN otp TEXT;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE bookings ADD COLUMN declined_driver_ids TEXT;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE bookings ADD COLUMN scheduled_time TEXT;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE bookings ADD COLUMN is_scheduled INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}

            // Drivers acceptance rate counters
            try {
                stmt.execute("ALTER TABLE drivers ADD COLUMN requests_received_count INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE drivers ADD COLUMN requests_accepted_count INTEGER DEFAULT 0;");
            } catch (SQLException ignored) {}

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
