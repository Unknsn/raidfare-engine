package com.ridefare.model;

/**
 * Represents environmental weather conditions and their corresponding demand impact factors.
 */
public enum WeatherCondition {
    CLEAR("Clear / Normal", 1.00),
    RAIN("Moderate Rain", 1.15),
    HEAVY_RAIN("Heavy Rain / Waterlogging", 1.30),
    STORM("Severe Storm / Extreme Weather", 1.50);

    private final String displayName;
    private final double impactMultiplier;

    WeatherCondition(String displayName, double impactMultiplier) {
        this.displayName = displayName;
        this.impactMultiplier = impactMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getImpactMultiplier() {
        return impactMultiplier;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
