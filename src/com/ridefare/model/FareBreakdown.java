package com.ridefare.model;

/**
 * Detailed itemized breakdown of a calculated ride fare.
 */
public class FareBreakdown {
    private final RideRequest request;
    private final MarketConditions conditions;
    private final double baseFare;
    private final double distanceCharge;
    private final double timeCharge;
    private final double normalFare;
    private final double effectiveDemandRatio;
    private final double surgeMultiplier;
    private final double finalFare;
    private final double surgeDelta;
    private final String demandLevel;
    private final String notes;

    public FareBreakdown(RideRequest request, MarketConditions conditions, double baseFare,
                         double distanceCharge, double timeCharge, double normalFare,
                         double effectiveDemandRatio, double surgeMultiplier, double finalFare,
                         String demandLevel, String notes) {
        this.request = request;
        this.conditions = conditions;
        this.baseFare = baseFare;
        this.distanceCharge = distanceCharge;
        this.timeCharge = timeCharge;
        this.normalFare = normalFare;
        this.effectiveDemandRatio = effectiveDemandRatio;
        this.surgeMultiplier = surgeMultiplier;
        this.finalFare = finalFare;
        this.surgeDelta = Math.max(0.0, finalFare - normalFare);
        this.demandLevel = demandLevel;
        this.notes = notes;
    }

    public RideRequest getRequest() {
        return request;
    }

    public MarketConditions getConditions() {
        return conditions;
    }

    public double getBaseFare() {
        return baseFare;
    }

    public double getDistanceCharge() {
        return distanceCharge;
    }

    public double getTimeCharge() {
        return timeCharge;
    }

    public double getNormalFare() {
        return normalFare;
    }

    public double getEffectiveDemandRatio() {
        return effectiveDemandRatio;
    }

    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public double getFinalFare() {
        return finalFare;
    }

    public double getSurgeDelta() {
        return surgeDelta;
    }

    public String getDemandLevel() {
        return demandLevel;
    }

    public String getNotes() {
        return notes;
    }

    public boolean hasSurge() {
        return surgeMultiplier > 1.0;
    }
}
