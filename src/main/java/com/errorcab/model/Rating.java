package com.errorcab.model;

import java.time.LocalDateTime;

/**
 * Driver rating left by a passenger after a completed ride.
 */
public class Rating {
    private int id;
    private int bookingId;
    private int driverId;
    private int passengerId;
    private int stars; // 1 to 5
    private String review;
    private LocalDateTime createdAt;

    public Rating(int id, int bookingId, int driverId, int passengerId, int stars, String review, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.driverId = driverId;
        this.passengerId = passengerId;
        this.stars = Math.max(1, Math.min(5, stars));
        this.review = review;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Rating(int bookingId, int driverId, int passengerId, int stars, String review) {
        this(0, bookingId, driverId, passengerId, stars, review, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = Math.max(1, Math.min(5, stars));
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
