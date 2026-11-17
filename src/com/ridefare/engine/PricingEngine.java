package com.ridefare.engine;

import com.ridefare.model.FareBreakdown;
import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;
import com.ridefare.model.VehicleType;
import com.ridefare.model.WeatherCondition;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * ============================================================================
 * Business Logic: PricingEngine.java
 * ============================================================================
 * 
 * WHAT THIS FILE DOES:
 * This is the central mathematical brain of the RideFare application.
 * It computes the dynamic surge multiplier and final payable ride fare in
 * Indian Rupees (₹) using a transparent, 3-phase calculation pipeline.
 * 
 * THE 3-PHASE CALCULATION PIPELINE:
 * 
 * ----------------------------------------------------------------------------
 * PHASE 1: Baseline Unsurged Fare Calculation (Standard Rates in ₹)
 * ----------------------------------------------------------------------------
 * Calculates the standard fare based on the car category, distance, and duration:
 *   Base Fare       = Starting fixed fee (₹50 for Mini, ₹60 for Sedan, ₹100 for SUV)
 *   Distance Charge = Distance (km) * PerKmRate
 *   Time Charge     = Duration (mins) * PerMinuteRate
 *   Normal Fare     = Base Fare + Distance Charge + Time Charge
 * 
 * ----------------------------------------------------------------------------
 * PHASE 2: Effective Demand Ratio (R_eff) Synthesis
 * ----------------------------------------------------------------------------
 * Measures the total demand pressure in the dispatch zone:
 *   Raw Ratio (R_raw) = Ride Requests / Available Drivers
 *   R_eff = R_raw * Peak Hour Multiplier * Weather Multiplier
 *   (Peak Hour adds 1.20x; Weather adds 1.00x for Clear up to 1.50x for Storm).
 * 
 * ----------------------------------------------------------------------------
 * PHASE 3: Piecewise Continuous Surge Multiplier Curve (S)
 * ----------------------------------------------------------------------------
 * Determines the multiplier based on the Effective Demand Ratio:
 *   1. Normal Demand (R_eff <= 1.0) : Multiplier = 1.00x (Standard fare)
 *   2. Mild Surge (1.0 < R_eff <= 1.5): Multiplier = 1.00 + 0.30 * (R_eff - 1.0)
 *   3. High Surge (1.5 < R_eff <= 2.5): Multiplier = 1.15 + 0.50 * (R_eff - 1.5)
 *   4. Extreme Surge (R_eff > 2.5)   : Multiplier = 1.65 + 0.65 * (R_eff - 2.5)
 *   5. Zero Drivers (Supply Outage)  : Multiplier = 3.50x (Maximum regulatory cap)
 * 
 * REGULATORY SAFEGUARD (MAXIMUM CAP):
 *   To protect passengers from algorithmic price gouging, the surge multiplier
 *   is strictly capped at a maximum of 3.50x.
 * 
 * WHY USE BIGDECIMAL INSTEAD OF DOUBLE?
 * Standard computers use binary floating-point representation (IEEE 754) for
 * 'double' and 'float'. Because numbers like 0.1 or 0.65 cannot be represented
 * exactly in binary, small rounding errors happen (e.g. 1.65 + 0.65 * 0.5 becomes
 * 1.9749999999999999 instead of 1.975). In banking and transport fares, this can
 * cause 1-paisa calculation errors. Using Java's BigDecimal with RoundingMode.HALF_UP
 * eliminates these errors and ensures exact financial accuracy.
 * ============================================================================
 */
public class PricingEngine {

    /** Minimum surge multiplier (fares never drop below 100% of standard tariff) */
    public static final double MIN_SURGE_MULTIPLIER = 1.00;

    /** Maximum statutory regulatory ceiling to prevent excessive surge (3.50x) */
    public static final double MAX_SURGE_MULTIPLIER = 3.50;

    /** Multiplier applied during morning and evening office rush hours (+20% demand) */
    public static final double PEAK_HOUR_MULTIPLIER = 1.20;

    /**
     * Calculates the complete itemized fare breakdown for a ride request.
     *
     * @param request    The passenger's trip details (pickup, drop, distance, duration, car)
     * @param conditions The current live market state (requests, drivers, peak, weather)
     * @return Fully itemized FareBreakdown record in Indian Rupees (₹)
     */
    public FareBreakdown calculateFare(RideRequest request, MarketConditions conditions) {
        // Step 1: Validate inputs before doing any math
        FareValidator.validateRideRequest(request);
        FareValidator.validateMarketConditions(conditions);

        VehicleType type = request.getVehicleType();

        // --------------------------------------------------------------------
        // PHASE 1: Baseline Unsurged Fare Calculation (in ₹)
        // --------------------------------------------------------------------
        BigDecimal base = BigDecimal.valueOf(type.getBaseFare());
        BigDecimal dist = BigDecimal.valueOf(request.getDistanceKm())
                .multiply(BigDecimal.valueOf(type.getPerKmRate()));
        BigDecimal time = BigDecimal.valueOf(request.getDurationMinutes())
                .multiply(BigDecimal.valueOf(type.getPerMinuteRate()));

        double baseFare = round(base.doubleValue());
        double distanceCharge = round(dist.doubleValue());
        double timeCharge = round(time.doubleValue());
        double normalFare = round(baseFare + distanceCharge + timeCharge);

        // --------------------------------------------------------------------
        // PHASE 2 & 3: Dynamic Surge Synthesis and Evaluation
        // --------------------------------------------------------------------
        double effectiveRatio = calculateEffectiveDemandRatio(conditions);
        SurgeEvaluation surgeEval = evaluateSurge(conditions, effectiveRatio);

        // --------------------------------------------------------------------
        // FINAL PAYABLE FARE ASSEMBLY
        // --------------------------------------------------------------------
        // Final Fare = Normal Fare * Surge Multiplier (rounded to 2 decimal places in ₹)
        double finalFare = round(BigDecimal.valueOf(normalFare)
                .multiply(BigDecimal.valueOf(surgeEval.multiplier))
                .doubleValue());

        // Return the complete itemized receipt
        return new FareBreakdown(
                request,
                conditions,
                baseFare,
                distanceCharge,
                timeCharge,
                normalFare,
                effectiveRatio,
                surgeEval.multiplier,
                finalFare,
                surgeEval.demandLevel,
                surgeEval.notes
        );
    }

    /**
     * Synthesizes the Effective Demand Ratio (R_eff).
     * 
     * Formula:
     *   R_eff = (Requests / Drivers) * PeakFactor * WeatherFactor
     * 
     * If availableDrivers is 0, returns Double.POSITIVE_INFINITY to cleanly
     * signal complete driver scarcity without causing a division by zero crash.
     */
    public double calculateEffectiveDemandRatio(MarketConditions conditions) {
        // Special case: Zero available drivers in the zone
        if (conditions.getAvailableDrivers() <= 0) {
            return Double.POSITIVE_INFINITY;
        }

        // Divide requests by drivers with 6 decimal places of precision
        BigDecimal req = BigDecimal.valueOf(conditions.getRideRequests());
        BigDecimal drv = BigDecimal.valueOf(conditions.getAvailableDrivers());
        BigDecimal rawRatio = req.divide(drv, 6, RoundingMode.HALF_UP);

        // Get peak hour factor (1.20x if true, 1.00x if false)
        BigDecimal peakFactor = BigDecimal.valueOf(conditions.isPeakHour() ? PEAK_HOUR_MULTIPLIER : 1.00);

        // Get weather factor (1.00x for Clear up to 1.50x for Storm)
        BigDecimal weatherFactor = BigDecimal.valueOf(conditions.getWeatherCondition().getImpactMultiplier());

        // Compound all factors together
        BigDecimal effective = rawRatio.multiply(peakFactor).multiply(weatherFactor);
        return round(effective.doubleValue());
    }

    /**
     * Simple helper class to store the intermediate outputs of surge evaluation.
     */
    public static class SurgeEvaluation {
        public final double multiplier;
        public final String demandLevel;
        public final String notes;

        public SurgeEvaluation(double multiplier, String demandLevel, String notes) {
            this.multiplier = multiplier;
            this.demandLevel = demandLevel;
            this.notes = notes;
        }
    }

    /**
     * Evaluates the piecewise linear surge curve against the effective demand ratio.
     *
     * @param conditions     Current market conditions
     * @param effectiveRatio The combined demand-to-supply ratio
     * @return SurgeEvaluation containing the multiplier, status text, and explanatory notes
     */
    public SurgeEvaluation evaluateSurge(MarketConditions conditions, double effectiveRatio) {
        // Safety Case: Zero drivers available (Complete Supply Outage)
        if (conditions.getAvailableDrivers() <= 0) {
            return new SurgeEvaluation(
                    MAX_SURGE_MULTIPLIER,
                    "Severe Supply Shortage (Zero Drivers)",
                    "Zero drivers currently available in the dispatch zone. Maximum surge cap (3.50x) enforced to attract drivers."
            );
        }

        BigDecimal ratio = BigDecimal.valueOf(effectiveRatio);
        BigDecimal rawMult;
        String demandLevel;
        String notes;

        // Piecewise curve logic
        if (effectiveRatio <= 1.0) {
            // Region 1: Demand is normal or drivers are abundant
            rawMult = BigDecimal.valueOf(MIN_SURGE_MULTIPLIER);
            demandLevel = "Normal Demand (No Surge)";
            notes = "Driver supply comfortably meets passenger requests. Standard baseline rates apply.";
        } else if (effectiveRatio <= 1.5) {
            // Region 2: Mild demand surge
            // Multiplier = 1.00 + 0.30 * (Ratio - 1.0)
            BigDecimal diff = ratio.subtract(BigDecimal.valueOf(1.0));
            rawMult = BigDecimal.valueOf(1.00).add(BigDecimal.valueOf(0.30).multiply(diff));
            demandLevel = "Mild Demand Surge";
            notes = "Slight uptick in booking activity; modest dynamic adjustment applied.";
        } else if (effectiveRatio <= 2.5) {
            // Region 3: High demand surge
            // Multiplier = 1.15 + 0.50 * (Ratio - 1.5)
            BigDecimal diff = ratio.subtract(BigDecimal.valueOf(1.5));
            rawMult = BigDecimal.valueOf(1.15).add(BigDecimal.valueOf(0.50).multiply(diff));
            demandLevel = "High Demand Surge";
            notes = "Noticeable imbalance between ride requests and active drivers in this zone.";
        } else {
            // Region 4: Extreme demand surge
            // Multiplier = 1.65 + 0.65 * (Ratio - 2.5)
            BigDecimal diff = ratio.subtract(BigDecimal.valueOf(2.5));
            rawMult = BigDecimal.valueOf(1.65).add(BigDecimal.valueOf(0.65).multiply(diff));
            demandLevel = "Extreme Demand Surge";
            notes = "Substantial shortage of drivers relative to passenger requests.";
        }

        // Clamp strictly between minimum (1.00x) and maximum regulatory cap (3.50x)
        BigDecimal minBound = BigDecimal.valueOf(MIN_SURGE_MULTIPLIER);
        BigDecimal maxBound = BigDecimal.valueOf(MAX_SURGE_MULTIPLIER);

        if (rawMult.compareTo(minBound) < 0) {
            rawMult = minBound;
        } else if (rawMult.compareTo(maxBound) > 0) {
            rawMult = maxBound;
        }

        double boundedMultiplier = round(rawMult.doubleValue());

        // Informative note if the statutory ceiling was enforced
        if (boundedMultiplier >= MAX_SURGE_MULTIPLIER) {
            demandLevel = "Maximum Surge Cap Enforced (3.50x)";
            notes = "Extreme demand pressure. Surge pricing is strictly capped at platform regulatory ceiling (3.50x).";
        }

        return new SurgeEvaluation(boundedMultiplier, demandLevel, notes);
    }

    /**
     * Utility method to round floating-point currency numbers to exactly 2 decimal
     * places using Half-Up rounding (commercial standard).
     *
     * @param value The raw double value
     * @return Rounded double value with 2 decimal precision
     */
    public static double round(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            return value;
        }
        return BigDecimal.valueOf(value)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
