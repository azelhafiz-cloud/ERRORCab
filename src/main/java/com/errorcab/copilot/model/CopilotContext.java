package com.errorcab.copilot.model;

import com.errorcab.model.Booking;
import com.errorcab.model.FavoriteLocation;
import com.errorcab.model.Passenger;
import com.errorcab.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates actual existing ERRORCab context for the passenger.
 * Uses only real project entities and data (Profile, Favorites, Bookings, Drivers).
 */
public class CopilotContext {
    private int passengerId;
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    private String defaultAddress;
    private int totalRides;
    private double totalSpent;
    private List<FavoriteLocation> favorites = new ArrayList<>();
    private List<Booking> recentTrips = new ArrayList<>();
    private Booking activeBooking;
    private int onlineDriversCount;
    private List<String> availableCabClasses = new ArrayList<>();
    private String resolvedPickupLocation;

    public CopilotContext() {}

    public CopilotContext(int passengerId, User user, List<FavoriteLocation> favorites,
                          List<Booking> recentTrips, Booking activeBooking,
                          int onlineDriversCount, List<String> availableCabClasses) {
        this.passengerId = passengerId;
        if (user != null) {
            this.passengerId = user.getId();
            this.passengerName = user.getName();
            this.passengerEmail = user.getEmail();
            this.passengerPhone = user.getPhone();
            if (user instanceof Passenger p) {
                this.defaultAddress = p.getDefaultAddress();
                this.totalRides = p.getTotalRides();
                this.totalSpent = p.getTotalSpent();
            } else {
                this.defaultAddress = "Kochi";
            }
        }
        this.favorites = favorites != null ? favorites : new ArrayList<>();
        this.recentTrips = recentTrips != null ? recentTrips : new ArrayList<>();
        this.activeBooking = activeBooking;
        this.onlineDriversCount = onlineDriversCount;
        this.availableCabClasses = availableCabClasses != null ? availableCabClasses : new ArrayList<>();

        // Resolve smart default pickup based on favorites or default address
        this.resolvedPickupLocation = resolveDefaultPickup();
    }

    private String resolveDefaultPickup() {
        if (favorites != null && !favorites.isEmpty()) {
            for (FavoriteLocation fav : favorites) {
                if ("Home".equalsIgnoreCase(fav.getLabel())) {
                    return fav.getLocationName();
                }
            }
            return favorites.get(0).getLocationName();
        }
        if (defaultAddress != null && !defaultAddress.trim().isEmpty()) {
            String clean = defaultAddress.split(",")[0].trim();
            if (!clean.isEmpty()) {
                return clean;
            }
        }
        return "Kakkanad";
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

    public String getPassengerEmail() {
        return passengerEmail;
    }

    public void setPassengerEmail(String passengerEmail) {
        this.passengerEmail = passengerEmail;
    }

    public String getPassengerPhone() {
        return passengerPhone;
    }

    public void setPassengerPhone(String passengerPhone) {
        this.passengerPhone = passengerPhone;
    }

    public String getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(String defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public int getTotalRides() {
        return totalRides;
    }

    public void setTotalRides(int totalRides) {
        this.totalRides = totalRides;
    }

    public double getTotalSpent() {
        return totalSpent;
    }

    public void setTotalSpent(double totalSpent) {
        this.totalSpent = totalSpent;
    }

    public List<FavoriteLocation> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<FavoriteLocation> favorites) {
        this.favorites = favorites != null ? favorites : new ArrayList<>();
    }

    public List<Booking> getRecentTrips() {
        return recentTrips;
    }

    public void setRecentTrips(List<Booking> recentTrips) {
        this.recentTrips = recentTrips != null ? recentTrips : new ArrayList<>();
    }

    public Booking getActiveBooking() {
        return activeBooking;
    }

    public void setActiveBooking(Booking activeBooking) {
        this.activeBooking = activeBooking;
    }

    public int getOnlineDriversCount() {
        return onlineDriversCount;
    }

    public void setOnlineDriversCount(int onlineDriversCount) {
        this.onlineDriversCount = onlineDriversCount;
    }

    public List<String> getAvailableCabClasses() {
        return availableCabClasses;
    }

    public void setAvailableCabClasses(List<String> availableCabClasses) {
        this.availableCabClasses = availableCabClasses != null ? availableCabClasses : new ArrayList<>();
    }

    public String getResolvedPickupLocation() {
        return resolvedPickupLocation;
    }

    public void setResolvedPickupLocation(String resolvedPickupLocation) {
        this.resolvedPickupLocation = resolvedPickupLocation;
    }
}
