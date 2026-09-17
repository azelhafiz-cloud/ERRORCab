package com.errorcab.model;

/**
 * Valid lifecycle states of an ERRORCab ride.
 * Implements strict transition rules as required by Section 23.
 */
public enum RideStatus {
    SCHEDULED("Scheduled", "#8B5CF6", 0),
    SEARCHING("Finding Driver", "#F59E0B", 1),
    DRIVER_ASSIGNED("Driver Assigned", "#3B82F6", 2),
    DRIVER_ARRIVING("Driver Arriving", "#6366F1", 3),
    RIDE_STARTED("Ride Started", "#10B981", 4),
    RIDE_COMPLETED("Ride Completed", "#059669", 5),
    CANCELLED("Cancelled", "#EF4444", 0);

    private final String displayName;
    private final String colorHex;
    private final int sequenceOrder;

    RideStatus(String displayName, String colorHex, int sequenceOrder) {
        this.displayName = displayName;
        this.colorHex = colorHex;
        this.sequenceOrder = sequenceOrder;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColorHex() {
        return colorHex;
    }

    public int getSequenceOrder() {
        return sequenceOrder;
    }

    /**
     * Checks if this status can transition to a target status.
     */
    public boolean canTransitionTo(RideStatus target) {
        if (target == CANCELLED) {
            // Cancellation only permitted during scheduled, initial and arriving states
            return this == SCHEDULED || this == SEARCHING || this == DRIVER_ASSIGNED || this == DRIVER_ARRIVING;
        }

        switch (this) {
            case SCHEDULED:
                return target == SEARCHING || target == DRIVER_ASSIGNED;
            case SEARCHING:
                return target == DRIVER_ASSIGNED;
            case DRIVER_ASSIGNED:
                return target == DRIVER_ARRIVING;
            case DRIVER_ARRIVING:
                return target == RIDE_STARTED;
            case RIDE_STARTED:
                return target == RIDE_COMPLETED;
            case RIDE_COMPLETED:
            case CANCELLED:
            default:
                return false; // Terminal states
        }
    }

    public boolean isTerminal() {
        return this == RIDE_COMPLETED || this == CANCELLED;
    }

    public boolean isCancellable() {
        return this == SCHEDULED || this == SEARCHING || this == DRIVER_ASSIGNED || this == DRIVER_ARRIVING;
    }
}
