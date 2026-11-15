package com.ridefare.model;

/**
 * Defines available ride categories and their specific pricing tariffs.
 */
public enum VehicleType {
    ECONOMY("Economy (Mini)", 2.50, 0.90, 0.15),
    STANDARD("Standard (Sedan)", 3.50, 1.25, 0.25),
    PREMIUM("Premium (SUV)", 5.50, 2.10, 0.40);

    private final String displayName;
    private final double baseFare;
    private final double perKmRate;
    private final double perMinuteRate;

    VehicleType(String displayName, double baseFare, double perKmRate, double perMinuteRate) {
        this.displayName = displayName;
        this.baseFare = baseFare;
        this.perKmRate = perKmRate;
        this.perMinuteRate = perMinuteRate;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getPerKmRate() {
        return perKmRate;
    }

    public double getPerMinuteRate() {
        return perMinuteRate;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
