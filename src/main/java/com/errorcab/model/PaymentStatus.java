package com.errorcab.model;

public enum PaymentStatus {
    PENDING("Pending", "#F59E0B"),
    PAID("Paid", "#10B981"),
    FAILED("Failed", "#EF4444");

    private final String displayName;
    private final String colorHex;

    PaymentStatus(String displayName, String colorHex) {
        this.displayName = displayName;
        this.colorHex = colorHex;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }
}
