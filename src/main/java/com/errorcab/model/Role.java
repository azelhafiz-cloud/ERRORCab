package com.errorcab.model;

/**
 * Three distinct user roles as specified in Section 1.
 */
public enum Role {
    PASSENGER("Passenger"),
    DRIVER("Driver"),
    ADMIN("Admin");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
