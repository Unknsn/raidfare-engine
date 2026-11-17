package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: VehicleType.java
 * ============================================================================
 * 
 * WHAT THIS FILE DOES:
 * This enum defines the 3 vehicle categories available in the ride-hailing app,
 * along with their base tariffs in Indian Rupees (₹).
 * 
 * WHY USE AN ENUM?
 * An 'enum' (enumeration) in Java is a special data type used to define a fixed
 * set of constants. Using an enum prevents spelling mistakes (like typing "sedan"
 * or "SEDAN" differently) and ensures only valid vehicle types can ever be chosen.
 * 
 * TARIFF RATE CARDS (INDIAN RUPEES - ₹):
 * ----------------------------------------------------------------------------
 * Category            | Base Fare (₹) | Per Km Rate (₹/km) | Per Min Rate (₹/min)
 * --------------------+---------------+--------------------+---------------------
 * Economy (Mini)      |    ₹ 50.00    |       ₹ 12.00      |        ₹ 1.50
 * Standard (Sedan)    |    ₹ 60.00    |       ₹ 15.00      |        ₹ 2.00
 * Premium (SUV)       |    ₹ 100.00   |       ₹ 22.00      |        ₹ 3.00
 * ----------------------------------------------------------------------------
 * 
 * HOW THE RATES WORK:
 * 1. Base Fare   : The starting fee charged when the cab arrives.
 * 2. Per Km Rate : The fee for each kilometer driven.
 * 3. Per Min Rate: The fee for each minute spent during the ride.
 * ============================================================================
 */
public enum VehicleType {

    /**
     * Compact entry-level vehicles (hatchbacks / autos / minis).
     * Budget-friendly option with lower baseline rates.
     */
    ECONOMY("Economy (Mini)", 50.00, 12.00, 1.50),

    /**
     * Mid-size comfortable sedans.
     * The standard daily commuting option in Indian cities.
     */
    STANDARD("Standard (Sedan)", 60.00, 15.00, 2.00),

    /**
     * Spacious luxury SUVs and larger vehicles.
     * Higher rates for extra comfort, luggage room, and group travel.
     */
    PREMIUM("Premium (SUV)", 100.00, 22.00, 3.00);

    /** User-friendly name displayed on screen (e.g. "Standard (Sedan)") */
    private final String displayName;

    /** Fixed starting fee in Indian Rupees (₹) */
    private final double baseFare;

    /** Fee per kilometer in Indian Rupees (₹/km) */
    private final double perKmRate;

    /** Fee per minute in Indian Rupees (₹/min) */
    private final double perMinuteRate;

    /**
     * Constructor to assign rate values to each vehicle category.
     */
    VehicleType(String displayName, double baseFare, double perKmRate, double perMinuteRate) {
        this.displayName = displayName;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.perMinuteRate = perMinuteRate;
    }

    /** Returns the formatted display name (e.g., "Economy (Mini)") */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the fixed base starting fare in ₹ */
    public double getBaseFare() {
        return baseFare;
    }

    /** Returns the rate per kilometer in ₹/km */
    public double getPerKmRate() {
        return perKmRate;
    }

    /** Returns the rate per minute in ₹/min */
    public double getPerMinuteRate() {
        return perMinuteRate;
    }

    /**
     * Converts this vehicle type to its display name for printing.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
