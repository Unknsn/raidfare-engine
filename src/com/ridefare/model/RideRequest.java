package com.ridefare.model;

/**
 * Encapsulates the user's ride booking details.
 */
public class RideRequest {
    private final String pickupLocation;
    private final String destination;
    private final double distanceKm;
    private final double durationMinutes;
    private final VehicleType vehicleType;

    public RideRequest(String pickupLocation, String destination, double distanceKm, 
                       double durationMinutes, VehicleType vehicleType) {
        this.pickupLocation = (pickupLocation != null) ? pickupLocation.trim() : "";
        this.destination = (destination != null) ? destination.trim() : "";
        this.distanceKm = distanceKm;
        this.durationMinutes = durationMinutes;
        this.vehicleType = (vehicleType != null) ? vehicleType : VehicleType.STANDARD;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public String getDestination() {
        return destination;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public double getDurationMinutes() {
        return durationMinutes;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    @Override
    public String toString() {
        return String.format("Ride from '%s' to '%s' (%.2f km, %.1f mins, %s)",
                pickupLocation, destination, distanceKm, durationMinutes, vehicleType.getDisplayName());
    }
}
