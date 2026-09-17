package com.errorcab.service;

import com.errorcab.model.Booking;
import com.errorcab.model.Driver;
import com.errorcab.model.RideStatus;
import com.errorcab.repository.BookingRepository;
import com.errorcab.repository.DriverRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service managing driver workflows, online status, and active ride constraints.
 */
@Service
public class DriverService {
    private final DriverRepository driverRepo = new DriverRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final RideService rideService = RideService.getInstance();

    public boolean setAvailability(int driverId, boolean online) {
        return driverRepo.updateOnlineStatus(driverId, online);
    }

    /**
     * Driver accepts an incoming ride.
     * Enforces the critical constraint:
     * A driver cannot accept another active ride while already handling a ride.
     */
    public synchronized boolean acceptRide(int driverId, int bookingId) {
        // 1. Verify driver is not already handling an active ride
        Optional<Booking> activeRide = bookingRepo.findActiveBookingForDriver(driverId);
        if (activeRide.isPresent() && activeRide.get().getId() != bookingId) {
            throw new IllegalStateException("You already have an active ride in progress (" +
                    activeRide.get().getBookingCode() + "). Please complete or resolve it before accepting another ride.");
        }

        // 2. Fetch driver
        Optional<Driver> driverOpt = driverRepo.findByUserId(driverId);
        if (driverOpt.isEmpty()) {
            throw new IllegalArgumentException("Driver profile not found.");
        }
        Driver driver = driverOpt.get();

        // 3. Fetch booking
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found.");
        }
        Booking booking = bookingOpt.get();

        if (booking.getStatus() != RideStatus.SEARCHING) {
            throw new IllegalStateException("This ride request has already been assigned or cancelled.");
        }

        return rideService.assignDriverToBooking(booking, driver);
    }

    public boolean updateRideStatus(int driverId, int bookingId, RideStatus targetStatus) {
        return updateRideStatus(driverId, bookingId, targetStatus, null);
    }

    public boolean updateRideStatus(int driverId, int bookingId, RideStatus targetStatus, String enteredOtp) {
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found.");
        }
        Booking booking = bookingOpt.get();

        if (booking.getDriverId() == null || booking.getDriverId() != driverId) {
            throw new IllegalStateException("You are not assigned to this ride.");
        }

        return rideService.advanceRideStatus(booking, targetStatus, enteredOtp);
    }

    public java.util.Map<String, Object> declineRide(int driverId, int bookingId) {
        return rideService.declineAndReassign(bookingId, driverId);
    }

    public java.util.Map<String, Object> getDriverEarningsAnalytics(int driverId) {
        Optional<Driver> driverOpt = driverRepo.findByUserId(driverId);
        double acceptanceRate = driverOpt.map(Driver::getAcceptanceRate).orElse(100.0);

        List<Booking> bookings = bookingRepo.getBookingsByDriver(driverId);
        List<Booking> completed = bookings.stream()
                .filter(b -> b.getStatus() == RideStatus.RIDE_COMPLETED)
                .toList();

        double totalEarnings = completed.stream().mapToDouble(Booking::getFare).sum();
        int completedCount = completed.size();
        double avgEarnings = completedCount > 0 ? (totalEarnings / completedCount) : 0.0;

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime startOfWeek = java.time.LocalDateTime.now().minusDays(7);
        java.time.LocalDateTime startOfMonth = java.time.LocalDateTime.now().minusDays(30);

        double todayEarnings = completed.stream()
                .filter(b -> b.getCompletedAt() != null && b.getCompletedAt().toLocalDate().isEqual(today))
                .mapToDouble(Booking::getFare).sum();

        double weekEarnings = completed.stream()
                .filter(b -> b.getCompletedAt() != null && b.getCompletedAt().isAfter(startOfWeek))
                .mapToDouble(Booking::getFare).sum();

        double monthEarnings = completed.stream()
                .filter(b -> b.getCompletedAt() != null && b.getCompletedAt().isAfter(startOfMonth))
                .mapToDouble(Booking::getFare).sum();

        // 7-day trend for visual chart
        java.util.List<java.util.Map<String, Object>> trend = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate d = today.minusDays(i);
            double dayTotal = completed.stream()
                    .filter(b -> b.getCompletedAt() != null && b.getCompletedAt().toLocalDate().isEqual(d))
                    .mapToDouble(Booking::getFare).sum();
            trend.add(java.util.Map.of(
                    "day", d.getDayOfWeek().name().substring(0, 3),
                    "date", d.toString(),
                    "amount", Math.round(dayTotal)
            ));
        }

        return java.util.Map.of(
                "todayEarnings", Math.round(todayEarnings * 100.0) / 100.0,
                "weekEarnings", Math.round(weekEarnings * 100.0) / 100.0,
                "monthEarnings", Math.round(monthEarnings * 100.0) / 100.0,
                "totalEarnings", Math.round(totalEarnings * 100.0) / 100.0,
                "completedRidesCount", completedCount,
                "averageFare", Math.round(avgEarnings * 100.0) / 100.0,
                "acceptanceRate", acceptanceRate,
                "dailyTrend", trend
        );
    }

    public Optional<Driver> getDriverProfile(int driverId) {
        return driverRepo.findByUserId(driverId);
    }
}
