package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: FareBreakdown.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Serves as the comprehensive, immutable financial receipt and audit ledger
 * produced by the PricingEngine.
 * 
 * TRANSPARENT PRICING PHILOSOPHY:
 * Commercial ride-hailing algorithms are often criticized as opaque "black boxes."
 * FareBreakdown provides full transparency by decomposing the total payable fare
 * into every individual contributing component:
 * 1. Base Flag-Fall Fare ($)
 * 2. Distance Traveled Charge ($)
 * 3. Journey Duration Time Charge ($)
 * 4. Normal Unsurged Subtotal ($)
 * 5. Effective Demand Ratio (R_eff)
 * 6. Piecewise Surge Multiplier (1.00x - 3.50x)
 * 7. Dynamic Surge Addition Delta ($)
 * 8. Final Payable Fare ($)
 * 9. Market Status Category and Transparent Diagnostic Notes
 * 
 * This object is consumed by both the Swing GraphicalUI and the Terminal
 * CommandLineUI to render itemized customer bills and diagnostic panels.
 * ============================================================================
 */
public class FareBreakdown {

    /** Original ride request parameters (locations, distance, vehicle tier) */
    private final RideRequest request;

    /** Market conditions snapshot at calculation time */
    private final MarketConditions conditions;

    /** Fixed vehicle category flag-fall fee ($) */
    private final double baseFare;

    /** Distance tariff charge: distanceKm * perKmRate ($) */
    private final double distanceCharge;

    /** Duration tariff charge: durationMinutes * perMinuteRate ($) */
    private final double timeCharge;

    /** Subtotal before dynamic surge adjustment: base + distance + time ($) */
    private final double normalFare;

    /** Synthesized demand ratio compounding supply, peak hour, and weather */
    private final double effectiveDemandRatio;

    /** Final dynamic multiplier applied to the normal fare (1.00x to 3.50x) */
    private final double surgeMultiplier;

    /** Total final payable price charged to the passenger ($) */
    private final double finalFare;

    /** Monetary surcharge added specifically due to surge: max(0, finalFare - normalFare) */
    private final double surgeDelta;

    /** Human-readable market classification (e.g. "Mild Surge", "Maximum Cap Enforced") */
    private final String demandLevel;

    /** Clear explanatory diagnostic narrative justifying the pricing adjustment */
    private final String notes;

    /**
     * Constructs a complete, itemized FareBreakdown record.
     *
     * @param request              Original ride booking details
     * @param conditions           Snapshot of market conditions
     * @param baseFare             Fixed base tariff component ($)
     * @param distanceCharge       Variable distance tariff component ($)
     * @param timeCharge           Variable duration tariff component ($)
     * @param normalFare           Standard unsurged baseline fare ($)
     * @param effectiveDemandRatio Calculated effective demand-supply ratio
     * @param surgeMultiplier      Piecewise surge multiplier applied (1.00x - 3.50x)
     * @param finalFare            Total rounded payable fare ($)
     * @param demandLevel          Categorical status string
     * @param notes                Transparent narrative explanation
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
        // Non-negative delta calculation
        this.surgeDelta = Math.max(0.0, finalFare - normalFare);
        this.demandLevel = demandLevel;
        this.notes = notes;
    }

    /**
     * Retrieves the ride request parameters.
     * @return RideRequest instance
     */
    public RideRequest getRequest() {
        return request;
    }

    /**
     * Retrieves the market conditions snapshot.
     * @return MarketConditions instance
     */
    public MarketConditions getConditions() {
        return conditions;
    }

    /**
     * Retrieves the base flag-fall fare.
     * @return Base fare in dollars
     */
    public double getBaseFare() {
        return baseFare;
    }

    /**
     * Retrieves the distance-based tariff component.
     * @return Distance charge in dollars
     */
    public double getDistanceCharge() {
        return distanceCharge;
    }

    /**
     * Retrieves the time-based tariff component.
     * @return Time charge in dollars
     */
    public double getTimeCharge() {
        return timeCharge;
    }

    /**
     * Retrieves the standard baseline normal fare (un-surged).
     * @return Normal fare in dollars
     */
    public double getNormalFare() {
        return normalFare;
    }

    /**
     * Retrieves the effective demand ratio (R_eff).
     * @return Effective demand ratio
     */
    public double getEffectiveDemandRatio() {
        return effectiveDemandRatio;
    }

    /**
     * Retrieves the evaluated surge multiplier.
     * @return Surge multiplier (1.00 to 3.50)
     */
    public double getSurgeMultiplier() {
        return surgeMultiplier;
    }

    /**
     * Retrieves the final rounded payable fare.
     * @return Total final fare in dollars
     */
    public double getFinalFare() {
        return finalFare;
    }

    /**
     * Retrieves the monetary difference between the surged fare and normal fare.
     * @return Surge delta in dollars (0.0 if un-surged)
     */
    public double getSurgeDelta() {
        return surgeDelta;
    }

    /**
     * Retrieves the human-readable market surge classification.
     * @return Descriptive level string
     */
    public String getDemandLevel() {
        return demandLevel;
    }

    /**
     * Retrieves the transparent diagnostic notes explaining the fare.
     * @return Informative diagnostic notes string
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Helper method indicating whether dynamic surge pricing was triggered.
     * @return True if surge multiplier > 1.00x, false otherwise
     */
    public boolean hasSurge() {
        return surgeMultiplier > 1.0;
    }
}
