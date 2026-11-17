package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: MarketConditions.java
 * ============================================================================
 * 
 * WHAT THIS FILE DOES:
 * Represents the live supply and demand situation in the area at booking time:
 * 1. rideRequests     : How many passengers are currently requesting rides.
 * 2. availableDrivers : How many free drivers are currently available.
 * 3. peakHour         : Whether it is rush hour (morning/evening office commute).
 * 4. weatherCondition : Clear, Rain, Heavy Rain, or Storm.
 * 
 * IMPORTANT SAFETY RULE (ZERO DRIVERS):
 * If availableDrivers is 0, dividing requests by drivers would normally cause a
 * mathematical crash (Division by Zero). To prevent this, getRawDemandRatio()
 * returns Double.POSITIVE_INFINITY. The PricingEngine detects this and cleanly
 * applies the maximum emergency cap (3.50x) instead of crashing.
 * ============================================================================
 */
public class MarketConditions {

    /** Number of active passenger booking requests */
    private final int rideRequests;

    /** Number of free drivers available in this dispatch area */
    private final int availableDrivers;

    /** True if it is currently rush hour (+20% demand weight) */
    private final boolean peakHour;

    /** Prevailing weather condition in the area */
    private final WeatherCondition weatherCondition;

    /**
     * Creates a new snapshot of current market conditions.
     */
    public MarketConditions(int rideRequests, int availableDrivers, boolean peakHour, 
                            WeatherCondition weatherCondition) {
        this.rideRequests = rideRequests;
        this.availableDrivers = availableDrivers;
        this.peakHour = peakHour;
        this.weatherCondition = (weatherCondition != null) ? weatherCondition : WeatherCondition.CLEAR;
    }

    /** Returns the count of ride requests */
    public int getRideRequests() {
        return rideRequests;
    }

    /** Returns the count of available drivers */
    public int getAvailableDrivers() {
        return availableDrivers;
    }

    /** Returns true if peak rush hour is active */
    public boolean isPeakHour() {
        return peakHour;
    }

    /** Returns the weather condition */
    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    /**
     * Calculates the raw ratio of demand to supply:
     *   Raw Ratio = Requests / Drivers
     * 
     * If drivers is 0, returns Double.POSITIVE_INFINITY to prevent a crash.
     */
    public double getRawDemandRatio() {
        if (availableDrivers <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) rideRequests / (double) availableDrivers;
    }

    /** Formats the market condition into a readable summary string */
    @Override
    public String toString() {
        return String.format("Market [Requests: %d, Drivers: %d, Peak: %b, Weather: %s]",
                rideRequests, availableDrivers, peakHour, weatherCondition.getDisplayName());
    }
}
