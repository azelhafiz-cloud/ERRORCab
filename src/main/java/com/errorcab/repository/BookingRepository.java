package com.errorcab.repository;

import com.errorcab.database.DatabaseManager;
import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.RideStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing ride bookings in SQLite.
 */
public class BookingRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public Booking createBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (booking_code, passenger_id, passenger_name, passenger_phone, " +
                "driver_id, driver_name, driver_phone, vehicle_model, vehicle_plate_number, driver_rating, " +
                "pickup_location, destination_location, distance_km, estimated_minutes, cab_type, fare, " +
                "status, cancellation_reason, created_at, completed_at, promo_code, discount, " +
                "otp, declined_driver_ids, scheduled_time, is_scheduled) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, booking.getBookingCode());
            ps.setInt(2, booking.getPassengerId());
            ps.setString(3, booking.getPassengerName());
            ps.setString(4, booking.getPassengerPhone());
            if (booking.getDriverId() != null) ps.setInt(5, booking.getDriverId()); else ps.setNull(5, Types.INTEGER);
            ps.setString(6, booking.getDriverName());
            ps.setString(7, booking.getDriverPhone());
            ps.setString(8, booking.getVehicleModel());
            ps.setString(9, booking.getVehiclePlateNumber());
            ps.setDouble(10, booking.getDriverRating());
            ps.setString(11, booking.getPickupLocation());
            ps.setString(12, booking.getDestinationLocation());
            ps.setDouble(13, booking.getDistanceKm());
            ps.setInt(14, booking.getEstimatedMinutes());
            ps.setString(15, booking.getCabType().name());
            ps.setDouble(16, booking.getFare());
            ps.setString(17, booking.getStatus().name());
            ps.setString(18, booking.getCancellationReason());
            ps.setString(19, booking.getCreatedAt().toString());
            ps.setString(20, booking.getCompletedAt() != null ? booking.getCompletedAt().toString() : null);
            ps.setString(21, booking.getPromoCode());
            ps.setDouble(22, booking.getDiscount());
            ps.setString(23, booking.getOtp());
            ps.setString(24, booking.getDeclinedDriverIds());
            ps.setString(25, booking.getScheduledTime());
            ps.setInt(26, booking.isScheduled() ? 1 : 0);

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    booking.setId(rs.getInt(1));
                }
            }
        }
        return booking;
    }

    public boolean assignDriver(int bookingId, int driverId, String driverName, String driverPhone,
                                String vehicleModel, String plateNumber, double driverRating, String otp) {
        String sql = "UPDATE bookings SET driver_id = ?, driver_name = ?, driver_phone = ?, " +
                "vehicle_model = ?, vehicle_plate_number = ?, driver_rating = ?, status = ?, otp = ? " +
                "WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            ps.setString(2, driverName);
            ps.setString(3, driverPhone);
            ps.setString(4, vehicleModel);
            ps.setString(5, plateNumber);
            ps.setDouble(6, driverRating);
            ps.setString(7, RideStatus.DRIVER_ASSIGNED.name());
            ps.setString(8, otp);
            ps.setInt(9, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean assignDriver(int bookingId, int driverId, String driverName, String driverPhone,
                                String vehicleModel, String plateNumber, double driverRating) {
        return assignDriver(bookingId, driverId, driverName, driverPhone, vehicleModel, plateNumber, driverRating, null);
    }

    public boolean unassignDriver(int bookingId, String declinedDriverIds) {
        String sql = "UPDATE bookings SET driver_id = NULL, driver_name = NULL, driver_phone = NULL, " +
                "vehicle_model = NULL, vehicle_plate_number = NULL, status = ?, declined_driver_ids = ?, otp = NULL " +
                "WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, RideStatus.SEARCHING.name());
            ps.setString(2, declinedDriverIds);
            ps.setInt(3, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int bookingId, RideStatus newStatus) {
        String sql = newStatus == RideStatus.RIDE_COMPLETED
                ? "UPDATE bookings SET status = ?, completed_at = ? WHERE id = ?"
                : "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus.name());
            if (newStatus == RideStatus.RIDE_COMPLETED) {
                ps.setString(2, LocalDateTime.now().toString());
                ps.setInt(3, bookingId);
            } else {
                ps.setInt(2, bookingId);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelBooking(int bookingId, String reason) {
        String sql = "UPDATE bookings SET status = ?, cancellation_reason = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, RideStatus.CANCELLED.name());
            ps.setString(2, reason);
            ps.setInt(3, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Optional<Booking> findById(int id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Booking> findActiveBookingForPassenger(int passengerId) {
        String sql = "SELECT * FROM bookings WHERE passenger_id = ? AND status NOT IN ('RIDE_COMPLETED', 'CANCELLED') ORDER BY id DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Optional<Booking> findActiveBookingForDriver(int driverId) {
        String sql = "SELECT * FROM bookings WHERE driver_id = ? AND status NOT IN ('RIDE_COMPLETED', 'CANCELLED') ORDER BY id DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Booking> findPendingRideRequests(CabType cabType) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = 'SEARCHING' AND cab_type = ? ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cabType.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getBookingsByPassenger(int passengerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE passenger_id = ? ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getBookingsByDriver(int driverId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE driver_id = ? ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, driverId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapBooking(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Integer driverId = rs.getInt("driver_id");
        if (rs.wasNull()) driverId = null;

        String createdStr = rs.getString("created_at");
        String compStr = rs.getString("completed_at");

        Booking b = new Booking(
                rs.getInt("id"),
                rs.getString("booking_code"),
                rs.getInt("passenger_id"),
                rs.getString("passenger_name"),
                rs.getString("passenger_phone"),
                driverId,
                rs.getString("driver_name"),
                rs.getString("driver_phone"),
                rs.getString("vehicle_model"),
                rs.getString("vehicle_plate_number"),
                rs.getDouble("driver_rating"),
                rs.getString("pickup_location"),
                rs.getString("destination_location"),
                rs.getDouble("distance_km"),
                rs.getInt("estimated_minutes"),
                CabType.valueOf(rs.getString("cab_type")),
                rs.getDouble("fare"),
                RideStatus.valueOf(rs.getString("status")),
                rs.getString("cancellation_reason"),
                createdStr != null ? LocalDateTime.parse(createdStr) : LocalDateTime.now(),
                compStr != null ? LocalDateTime.parse(compStr) : null
        );
        try {
            b.setPromoCode(rs.getString("promo_code"));
            b.setDiscount(rs.getDouble("discount"));
            b.setOtp(rs.getString("otp"));
            b.setDeclinedDriverIds(rs.getString("declined_driver_ids"));
            b.setScheduledTime(rs.getString("scheduled_time"));
            b.setScheduled(rs.getInt("is_scheduled") == 1);
        } catch (SQLException ignored) {}
        return b;
    }

    public List<Booking> getScheduledBookingsByPassenger(int passengerId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE passenger_id = ? AND is_scheduled = 1 AND status = 'SCHEDULED' ORDER BY id DESC";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, passengerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapBooking(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean cancelScheduledBooking(int bookingId) {
        String sql = "UPDATE bookings SET status = 'CANCELLED', cancellation_reason = 'Cancelled by passenger' WHERE id = ? AND status = 'SCHEDULED'";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
