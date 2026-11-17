package com.ridefare.engine;

import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;

/**
 * ============================================================================
 * Business Logic: FareValidator.java
 * ============================================================================
 * 
 * WHAT THIS FILE DOES:
 * Acts as the safety guard for the pricing engine. It checks every input
 * BEFORE any calculations take place to make sure all data makes sense.
 * 
 * WHY IS VALIDATION IMPORTANT?
 * In software engineering, "Garbage In, Garbage Out" is a common risk.
 * If a user accidentally enters a negative distance (-10 km) or types identical
 * pickup and destination points ("Airport" to "Airport"), calculating a fare
 * would be illogical.
 * 
 * RULES ENFORCED (BOUNDARY LIMITS):
 * 1. Locations        : Pickup and destination cannot be empty or identical.
 * 2. Minimum Distance : At least 0.1 km (prevents zero or negative distance trips).
 * 3. Maximum Distance : Up to 500.0 km (operational dispatch ceiling).
 * 4. Minimum Duration : At least 1.0 minute (prevents instant or negative travel times).
 * 5. Maximum Duration : Up to 1440.0 minutes (24 hours maximum continuous trip).
 * 6. Numbers          : Disallows NaN (Not-a-Number) and Infinity in inputs.
 * 7. Non-Negative     : Request and driver counts cannot be negative numbers (< 0).
 * 
 * If any rule is violated, it throws an IllegalArgumentException with a clear,
 * helpful message explaining exactly what was wrong.
 * ============================================================================
 */
public class FareValidator {

    /** Minimum allowable trip distance (0.1 km) */
    public static final double MIN_DISTANCE_KM = 0.1;

    /** Maximum allowable trip distance (500.0 km) */
    public static final double MAX_DISTANCE_KM = 500.0;

    /** Minimum allowable trip duration (1.0 minute) */
    public static final double MIN_DURATION_MINS = 1.0;

    /** Maximum allowable trip duration (1440.0 minutes = 24 hours) */
    public static final double MAX_DURATION_MINS = 1440.0;

    /**
     * Validates a RideRequest object.
     *
     * @param request the ride request to check
     * @throws IllegalArgumentException if any field is invalid
     */
    public static void validateRideRequest(RideRequest request) {
        // Check 1: Request object itself cannot be null
        if (request == null) {
            throw new IllegalArgumentException("Ride request cannot be null.");
        }

        // Check 2: Pickup location must not be blank
        if (request.getPickupLocation() == null || request.getPickupLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location cannot be empty.");
        }

        // Check 3: Destination must not be blank
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be empty.");
        }

        // Check 4: Pickup and destination cannot be the same place
        if (request.getPickupLocation().trim().equalsIgnoreCase(request.getDestination().trim())) {
            throw new IllegalArgumentException("Pickup location and destination cannot be identical.");
        }

        // Check 5: Distance must be a real finite number
        if (Double.isNaN(request.getDistanceKm()) || Double.isInfinite(request.getDistanceKm())) {
            throw new IllegalArgumentException("Distance must be a valid finite number.");
        }

        // Check 6: Distance must be at least 0.1 km
        if (request.getDistanceKm() < MIN_DISTANCE_KM) {
            throw new IllegalArgumentException(String.format(
                    "Distance must be at least %.1f km (received: %.2f km).",
                    MIN_DISTANCE_KM, request.getDistanceKm()));
        }

        // Check 7: Distance cannot exceed 500 km
        if (request.getDistanceKm() > MAX_DISTANCE_KM) {
            throw new IllegalArgumentException(String.format(
                    "Distance exceeds maximum supported distance of %.1f km (received: %.2f km).",
                    MAX_DISTANCE_KM, request.getDistanceKm()));
        }

        // Check 8: Duration must be a real finite number
        if (Double.isNaN(request.getDurationMinutes()) || Double.isInfinite(request.getDurationMinutes())) {
            throw new IllegalArgumentException("Duration must be a valid finite number.");
        }

        // Check 9: Duration must be at least 1.0 minute
        if (request.getDurationMinutes() < MIN_DURATION_MINS) {
            throw new IllegalArgumentException(String.format(
                    "Estimated duration must be at least %.1f minutes (received: %.1f mins).",
                    MIN_DURATION_MINS, request.getDurationMinutes()));
        }

        // Check 10: Duration cannot exceed 24 hours (1440 mins)
        if (request.getDurationMinutes() > MAX_DURATION_MINS) {
            throw new IllegalArgumentException(String.format(
                    "Estimated duration exceeds maximum limit of %.1f minutes.",
                    MAX_DURATION_MINS));
        }

        // Check 11: Vehicle type must be selected
        if (request.getVehicleType() == null) {
            throw new IllegalArgumentException("Vehicle type must be specified.");
        }
    }

    /**
     * Validates MarketConditions data integrity before calculating surge.
     *
     * @param conditions the market conditions to check
     * @throws IllegalArgumentException if counts are negative or objects are null
     */
    public static void validateMarketConditions(MarketConditions conditions) {
        // Check 1: Conditions object itself cannot be null
        if (conditions == null) {
            throw new IllegalArgumentException("Market conditions cannot be null.");
        }

        // Check 2: Requests count cannot be negative
        if (conditions.getRideRequests() < 0) {
            throw new IllegalArgumentException(String.format(
                    "Ride requests count cannot be negative (received: %d).",
                    conditions.getRideRequests()));
        }

        // Check 3: Drivers count cannot be negative
        if (conditions.getAvailableDrivers() < 0) {
            throw new IllegalArgumentException(String.format(
                    "Available drivers count cannot be negative (received: %d).",
                    conditions.getAvailableDrivers()));
        }

        // Check 4: Weather condition must be specified
        if (conditions.getWeatherCondition() == null) {
            throw new IllegalArgumentException("Weather condition must be specified.");
        }
    }
}
