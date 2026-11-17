package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: RideRequest.java
 * ============================================================================
 * 
 * WHAT THIS FILE DOES:
 * Holds the details of a single ride booking entered by the user:
 * - Where the passenger wants to be picked up (pickupLocation)
 * - Where they want to go (destination)
 * - How far the trip is in kilometers (distanceKm)
 * - How long the trip will take in minutes (durationMinutes)
 * - Which car category they selected (vehicleType: Economy, Standard, or Premium)
 * 
 * DESIGN:
 * This class is an "immutable object". All fields are 'final', meaning once the
 * object is created, its values cannot be accidentally changed by another part
 * of the program. This makes the code safe, reliable, and easy to understand.
 * ============================================================================
 */
public class RideRequest {

    /** Starting point of the trip */
    private final String pickupLocation;

    /** Destination point of the trip */
    private final String destination;

    /** Distance in kilometers (must be between 0.1 km and 500.0 km) */
    private final double distanceKm;

    /** Estimated duration in minutes (must be between 1.0 min and 1440.0 mins) */
    private final double durationMinutes;

    /** Chosen car category: Economy, Standard, or Premium */
    private final VehicleType vehicleType;

    /**
     * Creates a new RideRequest with the given trip details.
     * Trims spaces around text and defaults null values safely.
     */
    public RideRequest(String pickupLocation, String destination, double distanceKm, 
                       double durationMinutes, VehicleType vehicleType) {
        this.pickupLocation = (pickupLocation != null) ? pickupLocation.trim() : "";
        this.destination = (destination != null) ? destination.trim() : "";
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        // Default to Standard Sedan if none provided
        this.vehicleType = (vehicleType != null) ? vehicleType : VehicleType.STANDARD;
    }

    /** Returns the pickup location */
    public String getPickupLocation() {
        return pickupLocation;
    }

    /** Returns the destination */
    public String getDestination() {
        return destination;
    }

    /** Returns the distance in kilometers */
    public double getDistanceKm() {
        return distanceKm;
    }

    /** Returns the duration in minutes */
    public double getDurationMinutes() {
        return durationMinutes;
    }

    /** Returns the selected vehicle type */
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    /** Formats the ride request into a simple, readable summary string */
    @Override
    public String toString() {
        return String.format("Ride from '%s' to '%s' (%.2f km, %.1f mins, %s)",
                pickupLocation, destination, distanceKm, durationMinutes, vehicleType.getDisplayName());
    }
}
