package com.errorcab.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a cab ride booking in the ERRORCab system.
 * Demonstrates Encapsulation and Domain Integrity.
 */
public class Booking {
    private int id;
    private String bookingCode; // e.g. EC-1024
    private int passengerId;
    private String passengerName;
    private String passengerPhone;
    private Integer driverId;
    private String driverName;
    private String driverPhone;
    private String vehicleModel;
    private String vehiclePlateNumber;
    private double driverRating;
    private String pickupLocation;
    private String destinationLocation;
    private double distanceKm;
    private int estimatedMinutes;
    private CabType cabType;
    private double fare;
    private String promoCode;
    private double discount;
    private RideStatus status;
    private String cancellationReason;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");

    public Booking(int id, String bookingCode, int passengerId, String passengerName, String passengerPhone,
                   Integer driverId, String driverName, String driverPhone, String vehicleModel,
                   String vehiclePlateNumber, double driverRating, String pickupLocation,
                   String destinationLocation, double distanceKm, int estimatedMinutes,
                   CabType cabType, double fare, RideStatus status, String cancellationReason,
                   LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = id;
        this.bookingCode = bookingCode;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.passengerPhone = passengerPhone;
        this.driverId = driverId;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.vehicleModel = vehicleModel;
        this.vehiclePlateNumber = vehiclePlateNumber;
        this.driverRating = driverRating;
        this.pickupLocation = pickupLocation;
        this.destinationLocation = destinationLocation;
        this.distanceKm = distanceKm;
        this.estimatedMinutes = estimatedMinutes;
        this.cabType = cabType;
        this.fare = fare;
        this.status = status;
        this.cancellationReason = cancellationReason;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.completedAt = completedAt;
    }

    public Booking(int passengerId, String passengerName, String passengerPhone,
                   String pickupLocation, String destinationLocation,
                   double distanceKm, int estimatedMinutes, CabType cabType, double fare) {
        this(0, "EC-" + (1000 + (int) (Math.random() * 9000)), passengerId, passengerName, passengerPhone,
                null, null, null, null, null, 5.0,
                pickupLocation, destinationLocation, distanceKm, estimatedMinutes,
                cabType, fare, RideStatus.SEARCHING, null, LocalDateTime.now(), null);
    }

    public int getId() {
        return id;
    }

    public int getBookingId() {
        return id;
    }

    public String getDropoffLocation() {
        return destinationLocation;
    }

    public LocalDateTime getBookingTime() {
        return createdAt;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public int getPassengerId() {
        return passengerId;
    }

    public void setPassengerId(int passengerId) {
        this.passengerId = passengerId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public Integer getDriverId() {
        return driverId;
    }

    public void setDriverId(Integer driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehiclePlateNumber() {
        return vehiclePlateNumber;
    }

    public void setVehiclePlateNumber(String vehiclePlateNumber) {
        this.vehiclePlateNumber = vehiclePlateNumber;
    }

    public double getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(double driverRating) {
        this.driverRating = driverRating;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String destinationLocation) {
        this.destinationLocation = destinationLocation;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public int getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(int estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public CabType getCabType() {
        return cabType;
    }

    public void setCabType(CabType cabType) {
        this.cabType = cabType;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public RideStatus getStatus() {
        return status;
    }

    public void setStatus(RideStatus status) {
        this.status = status;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getFormattedDate() {
        return createdAt != null ? createdAt.format(FORMATTER) : "";
    }

    public String getRouteDisplay() {
        return pickupLocation + " → " + destinationLocation;
    }
}
