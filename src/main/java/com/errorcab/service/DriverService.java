package com.errorcab.service;

import com.errorcab.model.Booking;
import com.errorcab.model.Driver;
import com.errorcab.model.RideStatus;
import com.errorcab.repository.BookingRepository;
import com.errorcab.repository.DriverRepository;
import org.springframework.stereotype.Service;

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
        Optional<Booking> bookingOpt = bookingRepo.findById(bookingId);
        if (bookingOpt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found.");
        }
        Booking booking = bookingOpt.get();

        if (booking.getDriverId() == null || booking.getDriverId() != driverId) {
            throw new IllegalStateException("You are not assigned to this ride.");
        }

        return rideService.advanceRideStatus(booking, targetStatus);
    }

    public Optional<Driver> getDriverProfile(int driverId) {
        return driverRepo.findByUserId(driverId);
    }
}
