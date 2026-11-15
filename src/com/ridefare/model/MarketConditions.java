package com.ridefare.model;

/**
 * Encapsulates real-time supply, demand, and environmental conditions.
 */
public class MarketConditions {
    private final int rideRequests;
    private final int availableDrivers;
    private final boolean peakHour;
    private final WeatherCondition weatherCondition;

    public MarketConditions(int rideRequests, int availableDrivers, boolean peakHour, 
                            WeatherCondition weatherCondition) {
        this.rideRequests = rideRequests;
        this.availableDrivers = availableDrivers;
        this.peakHour = peakHour;
        this.weatherCondition = (weatherCondition != null) ? weatherCondition : WeatherCondition.CLEAR;
    }

    public int getRideRequests() {
        return rideRequests;
    }

    public int getAvailableDrivers() {
        return availableDrivers;
    }

    public boolean isPeakHour() {
        return peakHour;
    }

    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    /**
     * Calculates the raw demand-to-supply ratio.
     * Returns Double.POSITIVE_INFINITY if availableDrivers is 0.
     */
    public double getRawDemandRatio() {
        if (availableDrivers <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) rideRequests / (double) availableDrivers;
    }

    @Override
    public String toString() {
        return String.format("Market [Requests: %d, Drivers: %d, Peak: %b, Weather: %s]",
                rideRequests, availableDrivers, peakHour, weatherCondition.getDisplayName());
    }
}
