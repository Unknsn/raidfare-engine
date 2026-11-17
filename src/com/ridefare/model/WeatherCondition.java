package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: WeatherCondition.java
 * ============================================================================
 * 
 * WHAT THIS FILE DOES:
 * Represents weather conditions (Clear, Rain, Heavy Rain, Storm) and how much
 * they increase passenger demand and reduce driver availability.
 * 
 * WHY DOES WEATHER AFFECT PRICING?
 * When it rains heavily or a storm hits:
 * 1. More people want a cab instead of walking, cycling, or waiting for a bus.
 * 2. Fewer drivers want to drive due to traffic jams, waterlogging, and safety.
 * This creates a supply shortage, which the surge pricing engine accounts for.
 * 
 * MULTIPLIER TABLE:
 * ----------------------------------------------------------------------------
 * Weather Condition              | Multiplier | Meaning
 * -------------------------------+------------+------------------------------
 * Clear / Normal                 | 1.00x      | Normal weather, no adjustment
 * Moderate Rain                  | 1.15x      | +15% demand pressure
 * Heavy Rain / Waterlogging      | 1.30x      | +30% demand pressure
 * Severe Storm / Extreme Weather | 1.50x      | +50% demand pressure
 * ----------------------------------------------------------------------------
 * ============================================================================
 */
public enum WeatherCondition {

    /** Normal dry driving conditions */
    CLEAR("Clear / Normal", 1.00),

    /** Mild rain where commuters prefer a cab */
    RAIN("Moderate Rain", 1.15),

    /** Heavy rainfall with flooded roads and slower traffic */
    HEAVY_RAIN("Heavy Rain / Waterlogging", 1.30),

    /** Dangerous storm or cyclonic weather with severe driver shortages */
    STORM("Severe Storm / Extreme Weather", 1.50);

    /** Friendly title printed in terminal menus and receipts */
    private final String displayName;

    /** Multiplier applied to the demand ratio (1.00 to 1.50) */
    private final double impactMultiplier;

    /**
     * Constructor for weather conditions.
     */
    WeatherCondition(String displayName, double impactMultiplier) {
        this.displayName = displayName;
        this.impactMultiplier = impactMultiplier;
    }

    /** Returns the display name (e.g. "Heavy Rain / Waterlogging") */
    public String getDisplayName() {
        return displayName;
    }

    /** Returns the numerical impact multiplier */
    public double getImpactMultiplier() {
        return impactMultiplier;
    }

    /** Returns the display name when printed as text */
    @Override
    public String toString() {
        return displayName;
    }
}
