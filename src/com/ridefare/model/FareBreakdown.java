package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: FareBreakdown.java
 * ============================================================================
 * 
 * WHAT THIS FILE DOES:
 * Acts as the complete itemized receipt and pricing breakdown for a ride.
 * 
 * TRANSPARENCY IN PRICING:
 * Instead of giving the user a single mysterious number, this class breaks
 * down the final bill into every individual charge:
 * 1. Base Fare                  : Fixed starting charge (₹)
 * 2. Distance Charge            : Distance in km * rate per km (₹)
 * 3. Time Charge                : Duration in min * rate per min (₹)
 * 4. Normal Fare                : Base + Distance + Time (un-surged) (₹)
 * 5. Effective Demand Ratio     : Combined market ratio (Requests/Drivers * Peak * Weather)
 * 6. Surge Multiplier           : The multiplier applied (1.00x to 3.50x)
 * 7. Surge Delta (Extra Charge) : The additional surge amount (Final - Normal) (₹)
 * 8. Final Payable Fare         : Total amount the passenger pays (₹)
 * 9. Market Status & Notes      : Explanation of why surge was or wasn't applied
 * ============================================================================
 */
public class FareBreakdown {

    /** Original ride request inputs (locations, distance, duration, car tier) */
    private final RideRequest request;

    /** Market conditions snapshot at calculation time */
    private final MarketConditions conditions;

    /** Base starting tariff in ₹ */
    private final double baseFare;

    /** Distance charge in ₹ (km * rate/km) */
    private final double distanceCharge;

    /** Time charge in ₹ (mins * rate/min) */
    private final double timeCharge;

    /** Standard normal fare before any surge is applied (₹) */
    private final double normalFare;

    /** Combined effective demand-supply ratio (R_eff) */
    private final double effectiveDemandRatio;

    /** Evaluated dynamic surge multiplier (1.00x to 3.50x) */
    private final double surgeMultiplier;

    /** Final rounded total amount the passenger pays in Indian Rupees (₹) */
    private final double finalFare;

    /** Additional amount added purely due to surge: (Final Fare - Normal Fare) in ₹ */
    private final double surgeDelta;

    /** Human-readable status (e.g. "Normal Demand", "Mild Demand Surge", "Maximum Cap") */
    private final String demandLevel;

    /** Friendly explanatory message explaining the price adjustment */
    private final String notes;

    /**
     * Constructs a complete, itemized FareBreakdown record.
     */
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
        // Surge addition is always non-negative: max(0, finalFare - normalFare)
        this.surgeDelta = Math.max(0.0, finalFare - normalFare);
        this.demandLevel = demandLevel;
        this.notes = notes;
    }

    /** Returns the original ride request */
    public RideRequest getRequest() {
        return request;
    }

    /** Returns the market conditions */
    public MarketConditions getConditions() {
        return conditions;
    }

    /** Returns the base starting fare (₹) */
    public double getBaseFare() {
        return baseFare;
    }

    /** Returns the distance charge (₹) */
    public double getDistanceCharge() {
        return distanceCharge;
    }

    /** Returns the time duration charge (₹) */
    public double getTimeCharge() {
        return timeCharge;
    }

    /** Returns the normal un-surged fare (₹) */
    public double getNormalFare() {
        return normalFare;
    }

    /** Returns the effective demand-to-supply ratio */
    public double getEffectiveDemandRatio() {
        return effectiveDemandRatio;
    }

    /** Returns the surge multiplier (e.g. 1.00x, 1.40x, 3.50x) */
    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    /** Returns the total final payable fare in Indian Rupees (₹) */
    public double getFinalFare() {
        return finalFare;
    }

    /** Returns the extra surge addition in ₹ */
    public double getSurgeDelta() {
        return surgeDelta;
    }

    /** Returns the market status label */
    public String getDemandLevel() {
        return demandLevel;
    }

    /** Returns the transparent diagnostic explanation */
    public String getNotes() {
        return notes;
    }

    /** Helper method checking if surge pricing was active */
    public boolean hasSurge() {
        return surgeMultiplier > 1.0;
    }
}
