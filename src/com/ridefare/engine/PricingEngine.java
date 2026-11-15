package com.ridefare.engine;

import com.ridefare.model.FareBreakdown;
import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;
import com.ridefare.model.VehicleType;
import com.ridefare.model.WeatherCondition;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Core pricing engine that computes dynamic surge multipliers and itemized fares
 * using mathematically precise financial calculations.
 */
public class PricingEngine {

    public static final double MIN_SURGE_MULTIPLIER = 1.00;
    public static final double MAX_SURGE_MULTIPLIER = 3.50;
    public static final double PEAK_HOUR_MULTIPLIER = 1.20;

    /**
     * Calculates the complete itemized fare breakdown for a given ride request
     * and current market conditions.
     *
     * @param request    the ride request details
     * @param conditions the real-time market conditions
     * @return a complete FareBreakdown object
     */
    public FareBreakdown calculateFare(RideRequest request, MarketConditions conditions) {
        // Enforce input validation
        FareValidator.validateRideRequest(request);
        FareValidator.validateMarketConditions(conditions);

        VehicleType type = request.getVehicleType();

        // 1. Calculate standard un-surged fare components using BigDecimal for financial accuracy
        BigDecimal base = BigDecimal.valueOf(type.getBaseFare());
        BigDecimal dist = BigDecimal.valueOf(request.getDistanceKm())
                .multiply(BigDecimal.valueOf(type.getPerKmRate()));
        BigDecimal time = BigDecimal.valueOf(request.getDurationMinutes())
                .multiply(BigDecimal.valueOf(type.getPerMinuteRate()));

        double baseFare = round(base.doubleValue());
        double distanceCharge = round(dist.doubleValue());
        double timeCharge = round(time.doubleValue());
        double normalFare = round(baseFare + distanceCharge + timeCharge);

        // 2. Compute dynamic surge multiplier and market diagnostics
        double effectiveRatio = calculateEffectiveDemandRatio(conditions);
        SurgeEvaluation surgeEval = evaluateSurge(conditions, effectiveRatio);

        // 3. Compute final surged fare
        double finalFare = round(BigDecimal.valueOf(normalFare)
                .multiply(BigDecimal.valueOf(surgeEval.multiplier))
                .doubleValue());

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
     * Calculates the effective demand ratio combining raw supply-demand,
     * peak hour weighting, and weather impact.
     *
     * @param conditions the market conditions
     * @return effective demand ratio (or Double.POSITIVE_INFINITY if drivers == 0)
     */
    public double calculateEffectiveDemandRatio(MarketConditions conditions) {
        if (conditions.getAvailableDrivers() <= 0) {
            return Double.POSITIVE_INFINITY;
        }

        BigDecimal req = BigDecimal.valueOf(conditions.getRideRequests());
        BigDecimal drv = BigDecimal.valueOf(conditions.getAvailableDrivers());
        BigDecimal rawRatio = req.divide(drv, 6, RoundingMode.HALF_UP);

        BigDecimal peakFactor = BigDecimal.valueOf(conditions.isPeakHour() ? PEAK_HOUR_MULTIPLIER : 1.00);
        BigDecimal weatherFactor = BigDecimal.valueOf(conditions.getWeatherCondition().getImpactMultiplier());

        BigDecimal effective = rawRatio.multiply(peakFactor).multiply(weatherFactor);
        return round(effective.doubleValue());
    }

    /**
     * Internal container for surge evaluation results.
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
     * Evaluates the surge multiplier, demand level, and diagnostic notes.
     *
     * @param conditions     the market conditions
     * @param effectiveRatio the computed effective demand ratio
     * @return evaluation containing multiplier, level, and notes
     */
    public SurgeEvaluation evaluateSurge(MarketConditions conditions, double effectiveRatio) {
        // Handle supply exhaustion (zero drivers available)
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

        if (effectiveRatio <= 1.0) {
            // Supply meets or exceeds demand
            rawMult = BigDecimal.valueOf(MIN_SURGE_MULTIPLIER);
            demandLevel = "Normal Demand (No Surge)";
            notes = "Driver supply comfortably meets passenger requests. Standard baseline rates apply.";
        } else if (effectiveRatio <= 1.5) {
            // Mild demand increase (ratio between 1.0 and 1.5)
            // Multiplier = 1.00 + 0.30 * (Ratio - 1.0)
            BigDecimal diff = ratio.subtract(BigDecimal.valueOf(1.0));
            rawMult = BigDecimal.valueOf(1.00).add(BigDecimal.valueOf(0.30).multiply(diff));
            demandLevel = "Mild Demand Surge";
            notes = "Slight uptick in booking activity; modest dynamic adjustment applied.";
        } else if (effectiveRatio <= 2.5) {
            // High demand (ratio between 1.5 and 2.5)
            // Multiplier = 1.15 + 0.50 * (Ratio - 1.5)
            BigDecimal diff = ratio.subtract(BigDecimal.valueOf(1.5));
            rawMult = BigDecimal.valueOf(1.15).add(BigDecimal.valueOf(0.50).multiply(diff));
            demandLevel = "High Demand Surge";
            notes = "Noticeable imbalance between ride requests and active drivers in this zone.";
        } else {
            // Extreme demand (ratio > 2.5)
            // Multiplier = 1.65 + 0.65 * (Ratio - 2.5)
            BigDecimal diff = ratio.subtract(BigDecimal.valueOf(2.5));
            rawMult = BigDecimal.valueOf(1.65).add(BigDecimal.valueOf(0.65).multiply(diff));
            demandLevel = "Extreme Demand Surge";
            notes = "Substantial shortage of drivers relative to passenger requests.";
        }

        // Apply strict min and max boundaries
        BigDecimal minBound = BigDecimal.valueOf(MIN_SURGE_MULTIPLIER);
        BigDecimal maxBound = BigDecimal.valueOf(MAX_SURGE_MULTIPLIER);

        if (rawMult.compareTo(minBound) < 0) {
            rawMult = minBound;
        } else if (rawMult.compareTo(maxBound) > 0) {
            rawMult = maxBound;
        }

        double boundedMultiplier = round(rawMult.doubleValue());

        if (boundedMultiplier >= MAX_SURGE_MULTIPLIER) {
            demandLevel = "Maximum Surge Cap Enforced (3.50x)";
            notes = "Extreme demand pressure. Surge pricing is strictly capped at platform regulatory ceiling (3.50x).";
        }

        return new SurgeEvaluation(boundedMultiplier, demandLevel, notes);
    }

    /**
     * Utility method to round floating-point numbers to two decimal places
     * using Half-Up rounding for precise financial calculation.
     *
     * @param value the raw value
     * @return rounded value
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
