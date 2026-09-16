package com.errorcab.service;

import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.Passenger;
import com.errorcab.model.RideStatus;
import com.errorcab.repository.BookingRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Service managing booking creation, fare estimation, state transitions, and cancellation.
 */
@Service
public class BookingService {
    private final BookingRepository bookingRepo = new BookingRepository();
    private final RideService rideService = RideService.getInstance();
    private final FareService fareService = FareService.getInstance();
    private final PromoService promoService = new PromoService();

    public record FareEstimate(
            double distanceKm,
            int estimatedMinutes,
            double economyFare,
            double premiumFare,
            double suvFare,
            double baseFare,
            double perKmRate,
            double subtotal,
            double discount,
            double finalFare,
            String promoMessage
    ) {}

    public FareEstimate estimateFare(String pickup, String dest, CabType cabType, String promoCode) {
        double distance = MapService.getInstance().getDistanceKm(pickup, dest);
        int mins = MapService.getInstance().getEstimatedMinutes(distance);

        double eco = fareService.calculateFare(CabType.ECONOMY, distance);
        double prem = fareService.calculateFare(CabType.PREMIUM, distance);
        double suv = fareService.calculateFare(CabType.SUV, distance);

        double subtotal = fareService.calculateFare(cabType, distance);
        double discount = 0.0;
        double finalFare = subtotal;
        String promoMsg = "";

        if (promoCode != null && !promoCode.trim().isEmpty()) {
            PromoService.PromoResult pRes = promoService.applyPromo(promoCode, subtotal);
            if (pRes.valid()) {
                discount = pRes.discount();
                finalFare = pRes.finalFare();
                promoMsg = pRes.message();
            } else {
                promoMsg = pRes.message();
            }
        }

        var cab = fareService.getCab(cabType);

        return new FareEstimate(
                distance,
                mins,
                eco,
                prem,
                suv,
                cab.getBaseFare(),
                cab.getPerKmRate(),
                subtotal,
                discount,
                finalFare,
                promoMsg
        );
    }

    public Booking bookRide(Passenger passenger, String pickup, String dest, CabType cabType, String promoCode) throws SQLException {
        FareEstimate est = estimateFare(pickup, dest, cabType, promoCode);

        Booking booking = new Booking(
                passenger.getId(),
                passenger.getName(),
                passenger.getPhone(),
                pickup,
                dest,
                est.distanceKm(),
                est.estimatedMinutes(),
                cabType,
                est.finalFare()
        );

        if (est.discount() > 0) {
            booking.setPromoCode(promoCode.trim().toUpperCase());
            booking.setDiscount(est.discount());
        }

        booking = bookingRepo.createBooking(booking);

        // Auto-assign driver if available (or assign seeded demo driver)
        rideService.assignAvailableDriver(booking);

        return booking;
    }

    public boolean advanceStatus(int bookingId, RideStatus targetStatus) {
        Optional<Booking> opt = bookingRepo.findById(bookingId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Booking not found.");
        return rideService.advanceRideStatus(opt.get(), targetStatus);
    }

    public boolean cancelRide(int bookingId, String reason) {
        Optional<Booking> opt = bookingRepo.findById(bookingId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Booking not found.");
        return rideService.cancelRide(opt.get(), reason);
    }

    public Optional<Booking> getBooking(int id) {
        return bookingRepo.findById(id);
    }

    public Optional<Booking> getActiveBookingForPassenger(int passengerId) {
        return bookingRepo.findActiveBookingForPassenger(passengerId);
    }

    public Optional<Booking> getActiveBookingForDriver(int driverId) {
        return bookingRepo.findActiveBookingForDriver(driverId);
    }

    public List<Booking> getPassengerHistory(int passengerId) {
        return bookingRepo.getBookingsByPassenger(passengerId);
    }

    public List<Booking> getDriverHistory(int driverId) {
        return bookingRepo.getBookingsByDriver(driverId);
    }

    public List<Booking> getAllBookings() {
        return bookingRepo.getAllBookings();
    }
}
