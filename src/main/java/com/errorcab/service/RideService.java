package com.errorcab.service;

import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.RideStatus;
import com.errorcab.repository.BookingRepository;
import com.errorcab.repository.DriverRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Service managing ride booking lifecycle, driver assignment,
 * and state transitions according to strict business rules.
 */
public class RideService {
    private static RideService instance;
    private final BookingRepository bookingRepo = new BookingRepository();
    private final DriverRepository driverRepo = new DriverRepository();
    private final NotificationService notificationService = NotificationService.getInstance();
    private final List<Consumer<Booking>> rideUpdateListeners = new CopyOnWriteArrayList<>();

    private RideService() {}

    public static synchronized RideService getInstance() {
        if (instance == null) {
            instance = new RideService();
        }
        return instance;
    }

    public void addRideUpdateListener(Consumer<Booking> listener) {
        rideUpdateListeners.add(listener);
    }

    public void removeRideUpdateListener(Consumer<Booking> listener) {
        rideUpdateListeners.remove(listener);
    }

    private void notifyRideUpdated(Booking booking) {
        for (Consumer<Booking> listener : rideUpdateListeners) {
            try {
                listener.accept(booking);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Booking requestRide(Passenger passenger, String pickup, String dest, CabType cabType, double distanceKm, int estimatedMinutes, double fare) throws SQLException {
        Booking booking = new Booking(
                passenger.getId(),
                passenger.getName(),
                passenger.getPhone(),
                pickup,
                dest,
                distanceKm,
                estimatedMinutes,
                cabType,
                fare
        );

        booking = bookingRepo.createBooking(booking);
        notificationService.sendNotification(passenger.getId(), "Ride Requested", "Searching for top-rated " + cabType.getDisplayName() + " drivers near " + pickup + "...");
        notifyRideUpdated(booking);
        return booking;
    }

    /**
     * Finds and assigns an available online driver to the booking.
     */
    public boolean assignAvailableDriver(Booking booking) {
        List<Driver> available = driverRepo.findAvailableDrivers(booking.getCabType());
        if (available.isEmpty()) {
            // Fallback to any online driver if no exact cab type match
            available = driverRepo.getAllDrivers().stream().filter(Driver::isOnline).toList();
        }

        if (!available.isEmpty()) {
            Driver chosenDriver = available.get(0);
            return assignDriverToBooking(booking, chosenDriver);
        }
        return false;
    }

    public boolean assignDriverToBooking(Booking booking, Driver driver) {
        String otp = booking.getOtp();
        if (otp == null || otp.trim().isEmpty()) {
            otp = String.format("%04d", new java.security.SecureRandom().nextInt(10000));
            booking.setOtp(otp);
        }

        boolean success = bookingRepo.assignDriver(
                booking.getId(),
                driver.getId(),
                driver.getName(),
                driver.getPhone(),
                driver.getVehicle() != null ? driver.getVehicle().getModel() : "Maruti Suzuki Dzire",
                driver.getVehicle() != null ? driver.getVehicle().getPlateNumber() : "KL 07 AB 1234",
                driver.getRating(),
                otp
        );

        if (success) {
            driverRepo.recordAcceptedRequest(driver.getId());
            booking.setDriverId(driver.getId());
            booking.setDriverName(driver.getName());
            booking.setDriverPhone(driver.getPhone());
            booking.setVehicleModel(driver.getVehicle() != null ? driver.getVehicle().getModel() : "Maruti Suzuki Dzire");
            booking.setVehiclePlateNumber(driver.getVehicle() != null ? driver.getVehicle().getPlateNumber() : "KL 07 AB 1234");
            booking.setDriverRating(driver.getRating());
            booking.setStatus(RideStatus.DRIVER_ASSIGNED);

            notificationService.sendNotification(booking.getPassengerId(), "Driver Assigned! 🚖",
                    driver.getName() + " (" + booking.getVehiclePlateNumber() + ") accepted your ride request.");
            notificationService.sendNotification(driver.getId(), "New Ride Assigned",
                    "Pickup: " + booking.getPickupLocation() + " for " + booking.getPassengerName());

            notifyRideUpdated(booking);
        }
        return success;
    }

    /**
     * Advances ride status with validation.
     */
    public boolean advanceRideStatus(Booking booking, RideStatus targetStatus) {
        return advanceRideStatus(booking, targetStatus, null);
    }

    /**
     * Advances ride status with OTP validation for RIDE_STARTED.
     */
    public boolean advanceRideStatus(Booking booking, RideStatus targetStatus, String enteredOtp) {
        if (!booking.getStatus().canTransitionTo(targetStatus)) {
            throw new IllegalStateException("Invalid status transition from " + booking.getStatus() + " to " + targetStatus);
        }

        if (targetStatus == RideStatus.RIDE_STARTED) {
            if (booking.getOtp() != null && !booking.getOtp().trim().isEmpty()) {
                if (enteredOtp == null || !enteredOtp.trim().equals(booking.getOtp().trim())) {
                    throw new IllegalArgumentException("Invalid ride start OTP. Please verify OTP with passenger.");
                }
            }
        }

        boolean updated = bookingRepo.updateStatus(booking.getId(), targetStatus);
        if (updated) {
            booking.setStatus(targetStatus);

            switch (targetStatus) {
                case DRIVER_ARRIVING:
                    notificationService.sendNotification(booking.getPassengerId(), "Driver Arriving! ⏱",
                            booking.getDriverName() + " is arriving at your pickup location (" + booking.getPickupLocation() + ").");
                    break;
                case RIDE_STARTED:
                    notificationService.sendNotification(booking.getPassengerId(), "Ride Started 🟢",
                            "Enjoy your ride to " + booking.getDestinationLocation() + ". OTP verified.");
                    break;
                case RIDE_COMPLETED:
                    notificationService.sendNotification(booking.getPassengerId(), "Ride Completed! 🏁",
                            "You have arrived at " + booking.getDestinationLocation() + ". Total fare: ₹" + (int) booking.getFare());
                    if (booking.getDriverId() != null) {
                        driverRepo.recordCompletedTrip(booking.getDriverId(), booking.getFare());
                    }
                    break;
                default:
                    break;
            }

            notifyRideUpdated(booking);
        }
        return updated;
    }

    /**
     * Cancels a ride with a validated reason.
     */
    public boolean cancelRide(Booking booking, String reason) {
        if (!booking.getStatus().isCancellable()) {
            throw new IllegalStateException("Rides cannot be cancelled once in progress or completed.");
        }

        if (reason == null || reason.trim().isEmpty()) {
            reason = "Changed plans";
        }

        boolean success = bookingRepo.cancelBooking(booking.getId(), reason);
        if (success) {
            booking.setStatus(RideStatus.CANCELLED);
            booking.setCancellationReason(reason);

            notificationService.sendNotification(booking.getPassengerId(), "Ride Cancelled",
                    "Your booking " + booking.getBookingCode() + " has been cancelled (" + reason + ").");
            if (booking.getDriverId() != null) {
                notificationService.sendNotification(booking.getDriverId(), "Ride Cancelled by Passenger",
                        "Booking " + booking.getBookingCode() + " was cancelled.");
            }

            notifyRideUpdated(booking);
        }
        return success;
    }

    public Optional<Booking> getActiveBookingForPassenger(int passengerId) {
        return bookingRepo.findActiveBookingForPassenger(passengerId);
    }

    public Optional<Booking> getActiveBookingForDriver(int driverId) {
        return bookingRepo.findActiveBookingForDriver(driverId);
    }

    public Optional<Booking> getBookingById(int id) {
        return bookingRepo.findById(id);
    }

    public List<Booking> getPassengerBookings(int passengerId) {
        return bookingRepo.getBookingsByPassenger(passengerId);
    }

    public List<Booking> getDriverBookings(int driverId) {
        return bookingRepo.getBookingsByDriver(driverId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.getAllBookings();
    }

    public List<Booking> getPendingRequests(CabType cabType) {
        return bookingRepo.findPendingRideRequests(cabType);
    }

    public List<Booking> getPendingRequests(CabType cabType, Integer driverId) {
        List<Booking> list = bookingRepo.findPendingRideRequests(cabType);
        if (driverId != null) {
            return list.stream().filter(b -> !b.hasDriverDeclined(driverId)).toList();
        }
        return list;
    }

    public java.util.Map<String, Object> declineAndReassign(int bookingId, int driverId) {
        Optional<Booking> opt = bookingRepo.findById(bookingId);
        if (opt.isEmpty()) {
            throw new IllegalArgumentException("Booking not found.");
        }
        Booking booking = opt.get();
        driverRepo.recordDeclinedRequest(driverId);
        booking.addDeclinedDriver(driverId);
        bookingRepo.unassignDriver(bookingId, booking.getDeclinedDriverIds());
        booking.setDriverId(null);
        booking.setStatus(RideStatus.SEARCHING);

        notificationService.sendNotification(booking.getPassengerId(), "Finding New Driver 🚖",
                "Your driver was unavailable. We are searching for another driver for you...");

        // Find next available online driver
        List<Driver> candidates = driverRepo.findAvailableDrivers(booking.getCabType());
        if (candidates.isEmpty()) {
            candidates = driverRepo.getAllDrivers().stream().filter(Driver::isOnline).toList();
        }

        Driver nextDriver = null;
        for (Driver d : candidates) {
            if (!booking.hasDriverDeclined(d.getId())) {
                boolean hasActive = bookingRepo.getBookingsByDriver(d.getId()).stream().anyMatch(b ->
                        b.getStatus() == RideStatus.DRIVER_ASSIGNED ||
                        b.getStatus() == RideStatus.DRIVER_ARRIVING ||
                        b.getStatus() == RideStatus.RIDE_STARTED
                );
                if (!hasActive) {
                    nextDriver = d;
                    break;
                }
            }
        }

        if (nextDriver != null) {
            assignDriverToBooking(booking, nextDriver);
            return java.util.Map.of("success", true, "reassigned", true, "driverName", nextDriver.getName(), "bookingId", bookingId);
        } else {
            notificationService.sendNotification(booking.getPassengerId(), "Searching for Drivers ⌛",
                    "No other drivers currently available nearby. We will continue searching.");
            notifyRideUpdated(booking);
            return java.util.Map.of("success", true, "reassigned", false, "message", "No other drivers currently available", "bookingId", bookingId);
        }
    }
}
