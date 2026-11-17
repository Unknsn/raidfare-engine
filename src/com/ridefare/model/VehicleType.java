package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: VehicleType.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Represents the distinct categories (tiers) of vehicles available within the
 * ride-hailing platform, along with their standardized base tariff structures
 * calibrated for the Indian ride-hailing market (INR / ₹).
 * 
 * DESIGN PRINCIPLE:
 * Using a Java enum guarantees type safety across the entire codebase. It eliminates
 * "magic strings" and prevents invalid vehicle categories from ever entering
 * the calculation pipeline.
 * 
 * TARIFF RATE STRUCTURE (INDIAN RUPEES - ₹):
 * ----------------------------------------------------------------------------
 * Tier Category       | Base Fare (₹) | Per Km Rate (₹/km) | Per Min Rate (₹/min)
 * --------------------+---------------+--------------------+---------------------
 * Economy (Mini)      |    ₹ 50.00    |       ₹ 12.00      |        ₹ 1.50
 * Standard (Sedan)    |    ₹ 60.00    |       ₹ 15.00      |        ₹ 2.00
 * Premium (SUV)       |    ₹ 100.00   |       ₹ 22.00      |        ₹ 3.00
 * ----------------------------------------------------------------------------
 * 
 * Each tier encapsulates the specific cost structure reflecting operating expenses,
 * vehicle capital expenditure, fuel efficiency, and passenger comfort levels.
 * ============================================================================
 */
public enum VehicleType {

    /**
     * Compact entry-level vehicles (hatchbacks/minis/autos).
     * Budget-conscious pricing with the lowest baseline rates.
     */
    ECONOMY("Economy (Mini)", 50.00, 12.00, 1.50),

    /**
     * Standard mid-size commuter vehicles (sedans/prime).
     * Platform benchmark tariff representing typical daily passenger travel in Indian metros.
     */
    STANDARD("Standard (Sedan)", 60.00, 15.00, 2.00),

    /**
     * Luxury and high-capacity vehicles (SUVs/Prime XL).
     * Premium tariff for corporate travel, airport runs, larger groups, and extra luggage.
     */
    PREMIUM("Premium (SUV)", 100.00, 22.00, 3.00);

    /** Human-readable title displayed in UI dropdowns, CLI receipts, and reports */
    private final String displayName;

    /** Fixed baseline fee charged upon dispatch, regardless of journey distance (₹) */
    private final double baseFare;

    /** Distance-dependent variable tariff charged per traveled kilometer (₹/km) */
    private final double perKmRate;

    /** Time-dependent variable tariff charged per elapsed journey minute (₹/min) */
    private final double perMinuteRate;

    /**
     * Enum constructor binding fixed tariff parameters to each vehicle category.
     *
     * @param displayName   Formatted name for display purposes
     * @param baseFare      Fixed flag-fall base fee in Indian Rupees (₹)
     * @param perKmRate     Variable distance charge per kilometer (₹/km)
     * @param perMinuteRate Variable duration charge per minute (₹/min)
     */
    VehicleType(String displayName, double baseFare, double perKmRate, double perMinuteRate) {
        this.displayName = displayName;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.perMinuteRate = perMinuteRate;
    }

    /**
     * Retrieves the human-readable display label.
     * @return Formatted category string (e.g., "Standard (Sedan)")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retrieves the fixed baseline flag-fall fare.
     * @return Base fare amount in Indian Rupees (₹)
     */
    public double getBaseFare() {
        return baseFare;
    }

    /**
     * Retrieves the tariff charge per kilometer traveled.
     * @return Distance rate in Indian Rupees per kilometer (₹/km)
     */
    public double getPerKmRate() {
        return perKmRate;
    }

    /**
     * Retrieves the tariff charge per minute of journey time.
     * @return Time rate in Indian Rupees per minute (₹/min)
     */
    public double getPerMinuteRate() {
        return perMinuteRate;
    }

    /**
     * Formats enum instance for Swing components and string concatenation.
     * @return Display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
