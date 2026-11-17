package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: RideRequest.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Encapsulates the passenger's ride booking intent, containing all physical
 * journey parameters necessary to calculate the baseline un-surged fare.
 * 
 * DESIGN PATTERN — IMMUTABLE VALUE OBJECT:
 * - All instance fields are declared 'private final'.
 * - Once instantiated, the state cannot be modified, guaranteeing thread-safety
 *   across concurrent UI invocations and engine recalculations.
 * - Defensive sanitation: string arguments are trimmed and nulls are converted
 *   to safe defaults within the constructor.
 * 
 * INPUT ATTRIBUTES:
 * 1. pickupLocation   : Geographic origin point of the requested trip.
 * 2. destination      : Geographic destination point of the requested trip.
 * 3. distanceKm       : Total trip distance measured in kilometers (0.1 to 500.0 km).
 * 4. durationMinutes  : Estimated trip duration in minutes (1.0 to 1440.0 mins).
 * 5. vehicleType      : Desired vehicle category (ECONOMY, STANDARD, or PREMIUM).
 * ============================================================================
 */
public class RideRequest {

    /** Starting address or landmark of the ride */
    private final String pickupLocation;

    /** Destination address or landmark of the ride */
    private final String destination;

    /** Trip length in kilometers, measured by routing/mapping engine */
    private final double distanceKm;

    /** Estimated duration in minutes, accounting for normal traffic speeds */
    private final double durationMinutes;

    /** Chosen vehicle service tier determining the applicable tariff rate card */
    private final VehicleType vehicleType;

    /**
     * Constructs a validated, sanitized RideRequest instance.
     *
     * @param pickupLocation  Origin address (defensively trimmed)
     * @param destination     Destination address (defensively trimmed)
     * @param distanceKm      Distance in kilometers
     * @param durationMinutes Estimated travel duration in minutes
     * @param vehicleType     Selected vehicle category (defaults to STANDARD if null)
     */
    public RideRequest(String pickupLocation, String destination, double distanceKm, 
                       double durationMinutes, VehicleType vehicleType) {
        // Defensive trimming and null sanitation
        this.pickupLocation = (pickupLocation != null) ? pickupLocation.trim() : "";
        this.destination = (destination != null) ? destination.trim() : "";
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        // Default to Standard Sedan if caller passes a null reference
        this.vehicleType = (vehicleType != null) ? vehicleType : VehicleType.STANDARD;
    }

    /**
     * Retrieves the passenger's pickup location.
     * @return Trimmed pickup location string
     */
    public String getPickupLocation() {
        return pickupLocation;
    }

    /**
     * Retrieves the journey destination.
     * @return Trimmed destination string
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Retrieves the journey distance in kilometers.
     * @return Distance in km
     */
    public double getDistanceKm() {
        return distanceKm;
    }

    /**
     * Retrieves the estimated travel time in minutes.
     * @return Travel duration in minutes
     */
    public double getDurationMinutes() {
        return durationMinutes;
    }

    /**
     * Retrieves the selected vehicle service tier.
     * @return VehicleType enum constant
     */
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    /**
     * Formats the ride request into an informative summary string for logs and debugging.
     * @return Formatted summary string
     */
    @Override
    public String toString() {
        return String.format("Ride from '%s' to '%s' (%.2f km, %.1f mins, %s)",
                pickupLocation, destination, distanceKm, durationMinutes, vehicleType.getDisplayName());
    }
}
