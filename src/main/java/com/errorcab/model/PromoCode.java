package com.errorcab.model;

/**
 * Local promotional discount code entity.
 */
public class PromoCode {
    private int id;
    private String code;
    private double discountAmount;
    private double minimumFare;
    private String description;
    private boolean active;

    public PromoCode(int id, String code, double discountAmount, double minimumFare, String description, boolean active) {
        this.id = id;
        this.code = code != null ? code.toUpperCase().trim() : "";
        this.discountAmount = discountAmount;
        this.minimumFare = minimumFare;
        this.description = description;
        this.active = active;
    }

    public PromoCode(String code, double discountAmount, double minimumFare, String description) {
        this(0, code, discountAmount, minimumFare, description, true);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code != null ? code.toUpperCase().trim() : "";
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public double getMinimumFare() {
        return minimumFare;
    }

    public void setMinimumFare(double minimumFare) {
        this.minimumFare = minimumFare;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Calculates discount applicable for a given fare.
     */
    public double calculateDiscount(double subtotalFare) {
        if (!active || subtotalFare < minimumFare) {
            return 0.0;
        }
        return Math.min(discountAmount, subtotalFare - 30.0); // Ensures minimum token payment
    }
}
