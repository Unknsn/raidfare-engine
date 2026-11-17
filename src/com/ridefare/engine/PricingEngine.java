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
 * Business Logic Engine: PricingEngine.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * The computational core of RideFare. It evaluates dynamic surge multipliers
 * and generates itemized financial fare breakdowns by synthesizing journey
 * parameters, vehicle tariffs, real-time demand-supply balance, peak hours,
 * and environmental weather conditions.
 * 
 * MATHEMATICAL PRICING ARCHITECTURE (3-PHASE PIPELINE):
 * 
 * ----------------------------------------------------------------------------
 * PHASE 1: Baseline Unsurged Fare Calculation
 * ----------------------------------------------------------------------------
 * Computes standard baseline fare based on physical journey metrics and vehicle tier:
 *   Distance Charge = Distance (km) * PerKmRate
 *   Time Charge     = Duration (mins) * PerMinuteRate
 *   Normal Fare     = BaseFare + Distance Charge + Time Charge
 * 
 * ----------------------------------------------------------------------------
 * PHASE 2: Effective Demand Ratio (R_eff) Synthesis
 * ----------------------------------------------------------------------------
 * Synthesizes the overall market pressure from raw counts and external multipliers:
 *   Raw Ratio (R_raw) = RideRequests / AvailableDrivers
 *   R_eff = R_raw * PeakHourFactor * WeatherFactor
 *   (If AvailableDrivers == 0, R_eff is treated as Double.POSITIVE_INFINITY).
 * 
 * ----------------------------------------------------------------------------
 * PHASE 3: Piecewise Continuous Surge Multiplier S(R_eff) Evaluation
 * ----------------------------------------------------------------------------
 * The surge multiplier function is defined as a continuous piecewise linear curve:
 *   Region 1: R_eff <= 1.0           => S = 1.00x (Normal demand, no surge)
 *   Region 2: 1.0 < R_eff <= 1.5     => S = 1.00 + 0.30 * (R_eff - 1.0)
 *   Region 3: 1.5 < R_eff <= 2.5     => S = 1.15 + 0.50 * (R_eff - 1.5)
 *   Region 4: R_eff > 2.5            => S = 1.65 + 0.65 * (R_eff - 2.5)
 *   Special : AvailableDrivers == 0  => S = 3.50x (Total supply exhaustion)
 * 
 * STATUTORY CEILINGS & BOUNDARIES:
 *   S_clamped = min(3.50, max(1.00, S))
 *   Final Payable Fare = round(NormalFare * S_clamped)
 * 
 * WHY BIGDECIMAL IN FINANCIAL CALCULATIONS:
 * Standard primitive floats and doubles use IEEE 754 binary floating-point representation.
 * In IEEE 754, fractions like 0.1, 0.2, and 0.65 cannot be represented exactly in binary,
 * leading to subtle representation errors (e.g. 1.65 + 0.65 * 0.5 evaluates to 1.9749999999999999,
 * which rounds incorrectly to 1.97 instead of 1.98).
 * Using java.math.BigDecimal with RoundingMode.HALF_UP guarantees exact decimal precision
 * required for currency and regulatory compliance.
 * ============================================================================
 */
public class PricingEngine {

    /** Regulatory minimum multiplier (fares never drop below 100% of standard tariff) */
    public static final double MIN_SURGE_MULTIPLIER = 1.00;

    /** Statutory maximum multiplier ceiling to prevent excessive price gouging (350% cap) */
    public static final double MAX_SURGE_MULTIPLIER = 3.50;

    /** Temporal demand weight applied during peak rush hours (+20% demand pressure) */
    public static final double PEAK_HOUR_MULTIPLIER = 1.20;

    /**
     * Executes the complete 3-phase fare calculation pipeline.
     *
     * @param request    The passenger's ride booking parameters
     * @param conditions Current real-time market and environmental conditions
     * @return Fully itemized FareBreakdown record
     * @throws IllegalArgumentException if any input violates validation constraints
     */
    public FareBreakdown calculateFare(RideRequest request, MarketConditions conditions) {
        // Enforce defensive validation before performing any computations
        FareValidator.validateRideRequest(request);
        FareValidator.validateMarketConditions(conditions);

        VehicleType type = request.getVehicleType();

        // --------------------------------------------------------------------
        // PHASE 1: Baseline Unsurged Fare Calculation
        // --------------------------------------------------------------------
        // Convert rates and journey variables to BigDecimal to eliminate precision drift
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
        // PHASE 2 & 3: Surge Multiplier Synthesis & Piecewise Evaluation
        // --------------------------------------------------------------------
        double effectiveRatio = calculateEffectiveDemandRatio(conditions);
        SurgeEvaluation surgeEval = evaluateSurge(conditions, effectiveRatio);

        // --------------------------------------------------------------------
        // FINAL PRICING LEDGER ASSEMBLY
        // --------------------------------------------------------------------
        // Final Surged Fare = NormalFare * SurgeMultiplier (rounded to 2 decimal places)
        double finalFare = round(BigDecimal.valueOf(normalFare)
                .multiply(BigDecimal.valueOf(surgeEval.multiplier))
                .doubleValue());

        // Return the immutable itemized breakdown object
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
     * If availableDrivers is 0 or less, returns Double.POSITIVE_INFINITY to signal
     * complete supply exhaustion without causing an ArithmeticException.
     *
     * @param conditions Real-time market state
     * @return Effective demand ratio, or Double.POSITIVE_INFINITY if drivers == 0
     */
    public double calculateEffectiveDemandRatio(MarketConditions conditions) {
        // Edge Case: Total supply exhaustion (zero drivers available)
        if (conditions.getAvailableDrivers() <= 0) {
            return Double.POSITIVE_INFINITY;
        }

        // Calculate raw ratio using high-precision BigDecimal division (6 decimal places)
        BigDecimal req = BigDecimal.valueOf(conditions.getRideRequests());
        BigDecimal drv = BigDecimal.valueOf(conditions.getAvailableDrivers());
        BigDecimal rawRatio = req.divide(drv, 6, RoundingMode.HALF_UP);

        // Determine temporal and meteorological weighting factors
        BigDecimal peakFactor = BigDecimal.valueOf(conditions.isPeakHour() ? PEAK_HOUR_MULTIPLIER : 1.00);
        BigDecimal weatherFactor = BigDecimal.valueOf(conditions.getWeatherCondition().getImpactMultiplier());

        // Compound all factors into the effective ratio
        BigDecimal effective = rawRatio.multiply(peakFactor).multiply(weatherFactor);
        return round(effective.doubleValue());
    }

    /**
     * Immutable data container holding the intermediate outputs of surge evaluation.
     */
    public static class SurgeEvaluation {
        /** Final evaluated surge multiplier (clamped between 1.00x and 3.50x) */
        public final double multiplier;

        /** Human-readable status classification */
        public final String demandLevel;

        /** Descriptive explanatory notes detailing market justification */
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
     * Piecewise Logic:
     * - R_eff <= 1.0: Multiplier = 1.00x (Supply >= Demand; standard baseline rates).
     * - 1.0 < R_eff <= 1.5: Mild Surge => S = 1.00 + 0.30 * (R_eff - 1.0).
     * - 1.5 < R_eff <= 2.5: High Surge => S = 1.15 + 0.50 * (R_eff - 1.5).
     * - R_eff > 2.5: Extreme Surge => S = 1.65 + 0.65 * (R_eff - 2.5).
     * - Drivers == 0: Special case => S = 3.50x (Maximum regulatory ceiling).
     *
     * @param conditions     Market state snapshot
     * @param effectiveRatio Computed effective demand ratio
     * @return SurgeEvaluation containing bounded multiplier, status title, and notes
     */
    public SurgeEvaluation evaluateSurge(MarketConditions conditions, double effectiveRatio) {
        // Special Safeguard: Zero Drivers Available (Supply Exhaustion)
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

        // Piecewise curve segment routing
        if (effectiveRatio <= 1.0) {
            // Region 1: Balanced or over-supplied market
            rawMult = BigDecimal.valueOf(MIN_SURGE_MULTIPLIER);
            demandLevel = "Normal Demand (No Surge)";
            notes = "Driver supply comfortably meets passenger requests. Standard baseline rates apply.";
        } else if (effectiveRatio <= 1.5) {
            // Region 2: Mild demand pressure
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

        // Apply strict boundary enforcement (Clamping)
        BigDecimal minBound = BigDecimal.valueOf(MIN_SURGE_MULTIPLIER);
        BigDecimal maxBound = BigDecimal.valueOf(MAX_SURGE_MULTIPLIER);

        if (rawMult.compareTo(minBound) < 0) {
            rawMult = minBound; // Prevent multiplier dropping below 1.00x
        } else if (rawMult.compareTo(maxBound) > 0) {
            rawMult = maxBound; // Prevent multiplier exceeding regulatory cap of 3.50x
        }

        double boundedMultiplier = round(rawMult.doubleValue());

        // Update diagnostic message if statutory ceiling was reached
        if (boundedMultiplier >= MAX_SURGE_MULTIPLIER) {
            demandLevel = "Maximum Surge Cap Enforced (3.50x)";
            notes = "Extreme demand pressure. Surge pricing is strictly capped at platform regulatory ceiling (3.50x).";
        }

        return new SurgeEvaluation(boundedMultiplier, demandLevel, notes);
    }

    /**
     * Utility method to round floating-point numbers to exactly two decimal places
     * using Half-Up rounding (banker's / commercial currency standard).
     *
     * @param value Raw numerical value
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
