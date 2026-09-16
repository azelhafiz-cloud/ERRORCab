package com.errorcab.service;

import com.errorcab.model.Booking;
import com.errorcab.model.Rating;
import com.errorcab.repository.RatingRepository;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Service managing ride ratings and driver performance scores.
 */
public class RatingService {
    private static RatingService instance;
    private final RatingRepository ratingRepo = new RatingRepository();

    private RatingService() {}

    public static synchronized RatingService getInstance() {
        if (instance == null) {
            instance = new RatingService();
        }
        return instance;
    }

    public Rating submitRating(Booking booking, int stars, String review) throws SQLException {
        if (booking.getDriverId() == null) {
            throw new IllegalArgumentException("Cannot rate a booking with no assigned driver.");
        }
        if (ratingRepo.hasPassengerRated(booking.getId())) {
            throw new IllegalStateException("You have already submitted a rating for this ride.");
        }

        Rating rating = new Rating(
                booking.getId(),
                booking.getDriverId(),
                booking.getPassengerId(),
                stars,
                review != null ? review.trim() : ""
        );

        Rating saved = ratingRepo.submitRating(rating);

        NotificationService.getInstance().sendNotification(booking.getDriverId(), "New Rating Received ⭐",
                "You received a " + stars + "-star rating for ride " + booking.getBookingCode());

        return saved;
    }

    public boolean hasRated(int bookingId) {
        return ratingRepo.hasPassengerRated(bookingId);
    }

    public Optional<Rating> getRatingForBooking(int bookingId) {
        return ratingRepo.getRatingForBooking(bookingId);
    }
}
