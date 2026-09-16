package com.errorcab.model;

public enum PaymentMethod {
    CASH("Cash"),
    UPI("UPI (GPay / PhonePe / Paytm)"),
    CARD("Debit / Credit Card");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
