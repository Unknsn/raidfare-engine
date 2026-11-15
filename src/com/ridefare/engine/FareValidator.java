package com.ridefare.engine;

import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;

/**
 * Validates ride requests and market conditions to guarantee data integrity.
 */
public class FareValidator {

    public static final double MIN_DISTANCE_KM = 0.1;
    public static final double MAX_DISTANCE_KM = 500.0;
    public static final double MIN_DURATION_MINS = 1.0;
    public static final double MAX_DURATION_MINS = 1440.0; // 24 hours

    /**
     * Validates a RideRequest instance.
     *
     * @param request the request to validate
     * @throws IllegalArgumentException if any field is invalid
     */
    public static void validateRideRequest(RideRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Ride request cannot be null.");
        }

        if (request.getPickupLocation() == null || request.getPickupLocation().trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location cannot be empty.");
        }

        if (request.getDestination() == null || request.getDestination().trim().isEmpty()) {
            throw new IllegalArgumentException("Destination cannot be empty.");
        }

        if (request.getPickupLocation().trim().equalsIgnoreCase(request.getDestination().trim())) {
            throw new IllegalArgumentException("Pickup location and destination cannot be identical.");
        }

        if (Double.isNaN(request.getDistanceKm()) || Double.isInfinite(request.getDistanceKm())) {
            throw new IllegalArgumentException("Distance must be a valid finite number.");
        }

        if (request.getDistanceKm() < MIN_DISTANCE_KM) {
            throw new IllegalArgumentException(String.format(
                    "Distance must be at least %.1f km (received: %.2f km).",
                    MIN_DISTANCE_KM, request.getDistanceKm()));
        }

        if (request.getDistanceKm() > MAX_DISTANCE_KM) {
            throw new IllegalArgumentException(String.format(
                    "Distance exceeds maximum supported distance of %.1f km (received: %.2f km).",
                    MAX_DISTANCE_KM, request.getDistanceKm()));
        }

        if (Double.isNaN(request.getDurationMinutes()) || Double.isInfinite(request.getDurationMinutes())) {
            throw new IllegalArgumentException("Duration must be a valid finite number.");
        }

        if (request.getDurationMinutes() < MIN_DURATION_MINS) {
            throw new IllegalArgumentException(String.format(
                    "Estimated duration must be at least %.1f minutes (received: %.1f mins).",
                    MIN_DURATION_MINS, request.getDurationMinutes()));
        }

        if (request.getDurationMinutes() > MAX_DURATION_MINS) {
            throw new IllegalArgumentException(String.format(
                    "Estimated duration exceeds maximum limit of %.1f minutes.",
                    MAX_DURATION_MINS));
        }

        if (request.getVehicleType() == null) {
            throw new IllegalArgumentException("Vehicle type must be specified.");
        }
    }

    /**
     * Validates a MarketConditions instance.
     *
     * @param conditions the conditions to validate
     * @throws IllegalArgumentException if counts are negative or objects null
     */
    public static void validateMarketConditions(MarketConditions conditions) {
        if (conditions == null) {
            throw new IllegalArgumentException("Market conditions cannot be null.");
        }

        if (conditions.getRideRequests() < 0) {
            throw new IllegalArgumentException(String.format(
                    "Ride requests count cannot be negative (received: %d).",
                    conditions.getRideRequests()));
        }

        if (conditions.getAvailableDrivers() < 0) {
            throw new IllegalArgumentException(String.format(
                    "Available drivers count cannot be negative (received: %d).",
                    conditions.getAvailableDrivers()));
        }

        if (conditions.getWeatherCondition() == null) {
            throw new IllegalArgumentException("Weather condition must be specified.");
        }
    }
}
