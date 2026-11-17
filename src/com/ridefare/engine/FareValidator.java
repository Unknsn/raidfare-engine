package com.ridefare.engine;

import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;

/**
 * ============================================================================
 * Business Logic Engine: FareValidator.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Acts as the strict defensive gatekeeper for the pricing engine. It validates
 * all incoming RideRequest and MarketConditions objects before calculations begin,
 * protecting the system against erroneous, corrupted, or malicious inputs.
 * 
 * DEFENSIVE PROGRAMMING PRINCIPLES:
 * 1. Fail Fast: Invalid parameters are rejected immediately at the system boundary
 *    by throwing descriptive IllegalArgumentExceptions.
 * 2. Sanity Boundaries:
 *    - Minimum Distance: 0.1 km (prevents zero/negative trips; accounts for short hops).
 *    - Maximum Distance: 500.0 km (intra-city/inter-city operational limit).
 *    - Minimum Duration: 1.0 minute (prevents zero/instantaneous trips).
 *    - Maximum Duration: 1440.0 minutes (24-hour maximum continuous ride cutoff).
 * 3. Spatial Consistency: Pickup location and destination cannot be identical strings.
 * 4. Numeric Hygiene: Rejects Double.NaN and Double.POSITIVE_INFINITY in user fields.
 * 5. Non-Negativity: Active requests and drivers must be non-negative integers (>= 0).
 * ============================================================================
 */
public class FareValidator {

    /** Minimum allowable trip distance in kilometers */
    public static final double MIN_DISTANCE_KM = 0.1;

    /** Maximum operational dispatch distance in kilometers (500 km limit) */
    public static final double MAX_DISTANCE_KM = 500.0;

    /** Minimum journey duration in minutes */
    public static final double MIN_DURATION_MINS = 1.0;

    /** Maximum journey duration in minutes (24 hours = 1440 minutes) */
    public static final double MAX_DURATION_MINS = 1440.0;

    /**
     * Validates all fields of a RideRequest instance against system operational boundaries.
     *
     * @param request the RideRequest object containing trip parameters
     * @throws IllegalArgumentException if any field violates boundary or sanity rules
     */
    public static void validateRideRequest(RideRequest request) {
        // Guard 1: Null object check
        if (request == null) {
            throw new IllegalArgumentException("Ride request cannot be null.");
        }

        // Guard 2: Origin location must not be empty or blank
        if (request.getPickupLocation() == null || request.getPickupLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location cannot be empty.");
        }

        // Guard 3: Destination location must not be empty or blank
        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be empty.");
        }

        // Guard 4: Spatial origin and destination must differ
        if (request.getPickupLocation().trim().equalsIgnoreCase(request.getDestination().trim())) {
            throw new IllegalArgumentException("Pickup location and destination cannot be identical.");
        }

        // Guard 5: Distance must be a valid, finite number (no NaN or Infinity)
        if (Double.isNaN(request.getDistanceKm()) || Double.isInfinite(request.getDistanceKm())) {
            throw new IllegalArgumentException("Distance must be a valid finite number.");
        }

        // Guard 6: Distance lower boundary check
        if (request.getDistanceKm() < MIN_DISTANCE_KM) {
            throw new IllegalArgumentException(String.format(
                    "Distance must be at least %.1f km (received: %.2f km).",
                    MIN_DISTANCE_KM, request.getDistanceKm()));
        }

        // Guard 7: Distance upper operational boundary check
        if (request.getDistanceKm() > MAX_DISTANCE_KM) {
            throw new IllegalArgumentException(String.format(
                    "Distance exceeds maximum supported distance of %.1f km (received: %.2f km).",
                    MAX_DISTANCE_KM, request.getDistanceKm()));
        }

        // Guard 8: Duration must be a valid, finite number
        if (Double.isNaN(request.getDurationMinutes()) || Double.isInfinite(request.getDurationMinutes())) {
            throw new IllegalArgumentException("Duration must be a valid finite number.");
        }

        // Guard 9: Duration lower boundary check
        if (request.getDurationMinutes() < MIN_DURATION_MINS) {
            throw new IllegalArgumentException(String.format(
                    "Estimated duration must be at least %.1f minutes (received: %.1f mins).",
                    MIN_DURATION_MINS, request.getDurationMinutes()));
        }

        // Guard 10: Duration upper boundary check (24 hours)
        if (request.getDurationMinutes() > MAX_DURATION_MINS) {
            throw new IllegalArgumentException(String.format(
                    "Estimated duration exceeds maximum limit of %.1f minutes.",
                    MAX_DURATION_MINS));
        }

        // Guard 11: Vehicle tier must be explicitly defined
        if (request.getVehicleType() == null) {
            throw new IllegalArgumentException("Vehicle type must be specified.");
        }
    }

    /**
     * Validates MarketConditions data integrity before dynamic surge evaluation.
     *
     * @param conditions the MarketConditions object to validate
     * @throws IllegalArgumentException if counts are negative or objects are null
     */
    public static void validateMarketConditions(MarketConditions conditions) {
        // Guard 1: Null object check
        if (conditions == null) {
            throw new IllegalArgumentException("Market conditions cannot be null.");
        }

        // Guard 2: Request volume cannot be negative
        if (conditions.getRideRequests() < 0) {
            throw new IllegalArgumentException(String.format(
                    "Ride requests count cannot be negative (received: %d).",
                    conditions.getRideRequests()));
        }

        // Guard 3: Available driver supply cannot be negative
        if (conditions.getAvailableDrivers() < 0) {
            throw new IllegalArgumentException(String.format(
                    "Available drivers count cannot be negative (received: %d).",
                    conditions.getAvailableDrivers()));
        }

        // Guard 4: Weather condition must not be null
        if (conditions.getWeatherCondition() == null) {
            throw new IllegalArgumentException("Weather condition must be specified.");
        }
    }
}
