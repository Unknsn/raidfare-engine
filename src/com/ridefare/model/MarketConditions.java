package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: MarketConditions.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Encapsulates the real-time macroeconomic environment within a given dispatch
 * zone at the exact moment a ride quote is requested.
 * 
 * CORE MARKET ATTRIBUTES:
 * 1. rideRequests     : Integer count of active passengers seeking rides in the zone.
 * 2. availableDrivers : Integer count of unassigned, active drivers in the zone.
 * 3. peakHour         : Boolean flag indicating if current time falls into rush hour.
 * 4. weatherCondition : Current meteorological status (CLEAR, RAIN, HEAVY_RAIN, STORM).
 * 
 * ZERO-DRIVER SUPPLY DEFENSE:
 * In a real ride-hailing system, if driver supply drops to zero while passenger
 * requests remain positive, dividing requests by drivers results in division by zero.
 * Rather than throwing an unchecked ArithmeticException, getRawDemandRatio()
 * returns Double.POSITIVE_INFINITY. The PricingEngine detects this condition and
 * safely triggers the maximum statutory regulatory surge cap (3.50x).
 * ============================================================================
 */
public class MarketConditions {

    /** Total passenger ride requests currently queued in the dispatch sector */
    private final int rideRequests;

    /** Total drivers currently online, unbooked, and available for dispatch */
    private final int availableDrivers;

    /** Indicates whether booking time corresponds to morning/evening peak commute */
    private final boolean peakHour;

    /** Prevailing weather condition in the geographic dispatch area */
    private final WeatherCondition weatherCondition;

    /**
     * Constructs a validated, immutable snapshot of market conditions.
     *
     * @param rideRequests     Active booking request volume (>= 0)
     * @param availableDrivers Active free driver supply (>= 0)
     * @param peakHour         True if rush-hour surge multiplier applies
     * @param weatherCondition Meteorological state (defaults to CLEAR if null)
     */
    public MarketConditions(int rideRequests, int availableDrivers, boolean peakHour, 
                            WeatherCondition weatherCondition) {
        this.rideRequests = rideRequests;
        this.availableDrivers = availableDrivers;
        this.peakHour = peakHour;
        // Defensive null check: default to clear skies if not specified
        this.weatherCondition = (weatherCondition != null) ? weatherCondition : WeatherCondition.CLEAR;
    }

    /**
     * Retrieves the active ride request volume.
     * @return Integer passenger request count
     */
    public int getRideRequests() {
        return rideRequests;
    }

    /**
     * Retrieves the count of available drivers.
     * @return Integer active driver count
     */
    public int getAvailableDrivers() {
        return availableDrivers;
    }

    /**
     * Checks if peak hour traffic weighting is active.
     * @return True during peak hours (1.20x multiplier), false otherwise
     */
    public boolean isPeakHour() {
        return peakHour;
    }

    /**
     * Retrieves the prevailing weather condition.
     * @return WeatherCondition enum constant
     */
    public WeatherCondition getWeatherCondition() {
        return weatherCondition;
    }

    /**
     * Calculates the raw, unadjusted demand-to-supply ratio:
     * Raw Ratio = rideRequests / availableDrivers
     * 
     * Defensive Arithmetic:
     * If availableDrivers is 0 or less, returns Double.POSITIVE_INFINITY
     * to prevent an ArithmeticException / NaN and signal complete supply exhaustion.
     *
     * @return Unweighted ratio of demand to supply, or Double.POSITIVE_INFINITY
     */
    public double getRawDemandRatio() {
        if (availableDrivers <= 0) {
            return Double.POSITIVE_INFINITY;
        }
        return (double) rideRequests / (double) availableDrivers;
    }

    /**
     * Formats market conditions into an informative summary string for receipts and logs.
     * @return Formatted summary string
     */
    @Override
    public String toString() {
        return String.format("Market [Requests: %d, Drivers: %d, Peak: %b, Weather: %s]",
                rideRequests, availableDrivers, peakHour, weatherCondition.getDisplayName());
    }
}
