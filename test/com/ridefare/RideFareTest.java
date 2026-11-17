package com.ridefare;

import com.ridefare.engine.FareValidator;
import com.ridefare.engine.PricingEngine;
import com.ridefare.model.FareBreakdown;
import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;
import com.ridefare.model.VehicleType;
import com.ridefare.model.WeatherCondition;

/**
 * ============================================================================
 * Quality Assurance & Verification Suite: RideFareTest.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Provides a comprehensive, automated test harness that systematically verifies
 * the correctness, robustness, and mathematical integrity of the RideFare
 * Dynamic Surge Pricing Engine across all operational conditions, calibrated
 * in Indian Rupees (INR / ₹).
 * 
 * ZERO-DEPENDENCY DESIGN PHILOSOPHY:
 * Built strictly on the standard Java SE runtime without external testing
 * libraries (such as JUnit or TestNG). This guarantees that any evaluator,
 * student, or CI/CD pipeline can compile and execute the entire test suite
 * using a single 'javac' and 'java' command without dependency setup.
 * 
 * TEST COVERAGE CATEGORIES (41 TOTAL TESTS):
 * ----------------------------------------------------------------------------
 * 1. Baseline Fare Calculations   : Validates base tariff formulas in ₹ across tiers.
 * 2. Supply & Demand Dynamics     : Evaluates piecewise curve segments (1.00x - 3.50x).
 * 3. Environmental & Peak Factors : Verifies peak hour (1.20x) & weather multipliers.
 * 4. Edge Cases & Boundary Limits : Zero-driver defense, min/max distances, boundary transitions.
 * 5. Defensive Input Validation   : Rejection of nulls, empty strings, negatives, and invalid ranges.
 * 6. Determinism & Monotonicity   : 100-run reproducibility & mathematical monotonicity proof.
 * ============================================================================
 */
public class RideFareTest {

    /** Total count of test assertions executed */
    private static int totalTests = 0;

    /** Count of successful test assertions */
    private static int passedTests = 0;

    /** Count of failed test assertions */
    private static int failedTests = 0;

    /** Shared PricingEngine instance for test execution */
    private static final PricingEngine engine = new PricingEngine();

    /**
     * Test harness entry point. Executes all test suites sequentially, records
     * elapsed runtime, and prints a formatted test execution audit ledger.
     *
     * @param args Command-line arguments (unused)
     */
    public static void main(String[] args) {
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
            System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
        } catch (Exception ignored) {
        }
        System.out.println("================================================================================");
        System.out.println("              RIDEFARE AUTOMATED TEST SUITE EXECUTION (CP-044 - INR / ₹)        ");
        System.out.println("================================================================================");

        long startTime = System.currentTimeMillis();

        // Suite 1: Normal Baseline Fare Calculations & Vehicle Tariffs (INR / ₹)
        testNormalFareCalculation();
        testVehicleTiersTariffRates();

        // Suite 2: Supply and Demand Scenarios & Piecewise Multiplier Curve
        testLowDemandScenario();
        testBalancedMarketScenario();
        testMildSurgeScenario();
        testHighSurgeScenario();
        testExtremeSurgeScenario();
        testMaximumCapEnforcement();

        // Suite 3: Environmental & Temporal Factors Compounding
        testPeakHourSurgeCompounding();
        testWeatherConditionsMultiplier();
        testCombinedPeakAndSevereStorm();

        // Suite 4: Edge Cases & Operational Boundary Conditions
        testZeroAvailableDriversSupplyExhaustion();
        testZeroRideRequests();
        testMinimumDistanceBoundary();
        testMaximumDistanceBoundary();
        testExactSurgeThresholdBoundaries();

        // Suite 5: Defensive Input Validation & Exception Handling
        testValidationEmptyPickupLocation();
        testValidationEmptyDestination();
        testValidationIdenticalLocations();
        testValidationNegativeDistance();
        testValidationZeroDistance();
        testValidationNegativeDuration();
        testValidationNegativeRequests();
        testValidationNegativeDrivers();

        // Suite 6: Determinism & Mathematical Consistency
        testCalculationDeterminism();
        testMonotonicityOfSurgeMultiplier();

        long elapsed = System.currentTimeMillis() - startTime;

        // Print Formatted Test Execution Audit Ledger
        System.out.println("\n================================================================================");
        System.out.println("                             TEST EXECUTION SUMMARY                             ");
        System.out.println("================================================================================");
        System.out.printf("Total Test Cases Executed: %d%n", totalTests);
        System.out.printf("Passed:                    %d (%.1f%%)%n", passedTests, (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0));
        System.out.printf("Failed:                    %d%n", failedTests);
        System.out.printf("Execution Time:            %d ms%n", elapsed);
        System.out.println("================================================================================");

        // Terminate with non-zero exit status if any test fails
        if (failedTests == 0) {
            System.out.println("[SUCCESS] ALL TEST CASES PASSED WITH 100% INTEGRITY.");
        } else {
            System.err.println("[FAILURE] SOME TEST CASES FAILED. REVIEW SYSTEM LOGS.");
            System.exit(1);
        }
    }

    /**
     * Primary assertion helper: verifies boolean condition and logs formatted result.
     *
     * @param testName  Short descriptive name of the test case
     * @param condition Evaluated boolean result
     * @param details   Contextual diagnostics, expected vs actual values
     */
    private static void assertTrue(String testName, boolean condition, String details) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.printf("  [PASS] %-45s | %s%n", testName, details);
        } else {
            failedTests++;
            System.err.printf("  [FAIL] %-45s | %s%n", testName, details);
        }
    }

    /**
     * Floating-point assertion helper: verifies equality within a strict delta epsilon.
     *
     * @param testName Short descriptive name of the test case
     * @param expected Mathematically expected double value
     * @param actual   Engine-produced double value
     * @param delta    Allowed tolerance window (e.g. 0.01 for currency, 0.001 for ratios)
     * @param details  Explanation of the expected formula
     */
    private static void assertEqualDoubles(String testName, double expected, double actual, double delta, String details) {
        boolean match = Math.abs(expected - actual) <= delta;
        assertTrue(testName, match, String.format("Expected=%.2f, Actual=%.2f | %s", expected, actual, details));
    }

    // ========================================================================
    // SUITE 1: BASELINE UNSURGED FARE CALCULATIONS (INDIAN RUPEES - ₹)
    // ========================================================================

    /**
     * Validates baseline un-surged fare calculations for a Standard Sedan:
     * Base: ₹60.00 + Distance (10 km * ₹15.00 = ₹150.00) + Time (20 min * ₹2.00 = ₹40.00) = ₹250.00.
     */
    private static void testNormalFareCalculation() {
        System.out.println("\n--- Category 1: Baseline Fare Calculations (INR / ₹) ---");
        RideRequest req = new RideRequest("Point A", "Point B", 10.0, 20.0, VehicleType.STANDARD);
        MarketConditions cond = new MarketConditions(10, 20, false, WeatherCondition.CLEAR);

        FareBreakdown b = engine.calculateFare(req, cond);
        assertEqualDoubles("Standard Fare Un-surged", 250.00, b.getNormalFare(), 0.01, "Base ₹60.00 + Dist ₹150.00 + Time ₹40.00");
        assertEqualDoubles("Standard Multiplier 1.00x", 1.00, b.getSurgeMultiplier(), 0.001, "Normal demand ratio 0.50");
        assertEqualDoubles("Final Fare Equals Normal Fare", 250.00, b.getFinalFare(), 0.01, "No surge delta added");
    }

    /**
     * Validates tariff schedules for Economy and Premium vehicle tiers in ₹:
     * Economy: Base ₹50.00 + (10 * ₹12.00 = ₹120.00) + (20 * ₹1.50 = ₹30.00) = ₹200.00
     * Premium: Base ₹100.00 + (10 * ₹22.00 = ₹220.00) + (20 * ₹3.00 = ₹60.00) = ₹380.00
     */
    private static void testVehicleTiersTariffRates() {
        RideRequest reqEco = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.ECONOMY);
        RideRequest reqPrem = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.PREMIUM);
        MarketConditions cond = new MarketConditions(10, 10, false, WeatherCondition.CLEAR);

        FareBreakdown bEco = engine.calculateFare(reqEco, cond);
        assertEqualDoubles("Economy Tier Tariff", 200.00, bEco.getNormalFare(), 0.01, "Economy 10km, 20min (₹200.00)");

        FareBreakdown bPrem = engine.calculateFare(reqPrem, cond);
        assertEqualDoubles("Premium Tier Tariff", 380.00, bPrem.getNormalFare(), 0.01, "Premium 10km, 20min (₹380.00)");
    }

    // ========================================================================
    // SUITE 2: SUPPLY AND DEMAND DYNAMICS & PIECEWISE MULTIPLIER
    // ========================================================================

    /**
     * Validates low demand scenario (ratio = 0.25 <= 1.0 => Multiplier = 1.00x).
     */
    private static void testLowDemandScenario() {
        System.out.println("\n--- Category 2: Supply and Demand Dynamics ---");
        MarketConditions cond = new MarketConditions(10, 40, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Low Demand Multiplier", 1.00, b.getSurgeMultiplier(), 0.01, "Ratio 0.25 <= 1.0");
        assertTrue("Low Demand Level Description", b.getDemandLevel().contains("Normal Demand"), b.getDemandLevel());
    }

    /**
     * Validates balanced market scenario (ratio = 1.00 <= 1.0 => Multiplier = 1.00x).
     */
    private static void testBalancedMarketScenario() {
        MarketConditions cond = new MarketConditions(20, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Balanced Market Multiplier", 1.00, b.getSurgeMultiplier(), 0.01, "Ratio 1.00 <= 1.0");
    }

    /**
     * Validates mild surge scenario (ratio = 1.30):
     * Multiplier = 1.00 + 0.30 * (1.30 - 1.0) = 1.09x.
     */
    private static void testMildSurgeScenario() {
        MarketConditions cond = new MarketConditions(26, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Mild Surge Multiplier", 1.09, b.getSurgeMultiplier(), 0.01, "Ratio 1.30 -> S=1.09");
        assertTrue("Mild Surge Label", b.getDemandLevel().contains("Mild"), b.getDemandLevel());
    }

    /**
     * Validates high surge scenario (ratio = 2.00):
     * Multiplier = 1.15 + 0.50 * (2.00 - 1.5) = 1.40x.
     */
    private static void testHighSurgeScenario() {
        MarketConditions cond = new MarketConditions(40, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("High Surge Multiplier", 1.40, b.getSurgeMultiplier(), 0.01, "Ratio 2.00 -> S=1.40");
        assertTrue("High Surge Label", b.getDemandLevel().contains("High"), b.getDemandLevel());
    }

    /**
     * Validates extreme surge scenario (ratio = 3.00):
     * Multiplier = 1.65 + 0.65 * (3.00 - 2.5) = 1.975 -> 1.98x (Half-Up rounding).
     */
    private static void testExtremeSurgeScenario() {
        MarketConditions cond = new MarketConditions(60, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Extreme Surge Multiplier", 1.98, b.getSurgeMultiplier(), 0.01, "Ratio 3.00 -> S=1.98");
        assertTrue("Extreme Surge Label", b.getDemandLevel().contains("Extreme"), b.getDemandLevel());
    }

    /**
     * Validates statutory regulatory ceiling enforcement:
     * When calculated multiplier exceeds 3.50x, it is strictly clamped to 3.50x.
     */
    private static void testMaximumCapEnforcement() {
        MarketConditions cond = new MarketConditions(200, 10, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Maximum Cap Multiplier", 3.50, b.getSurgeMultiplier(), 0.001, "Strict cap at 3.50x");
        assertTrue("Maximum Cap Enforced Label", b.getDemandLevel().contains("Maximum Surge Cap"), b.getDemandLevel());
    }

    // ========================================================================
    // SUITE 3: ENVIRONMENTAL & TEMPORAL FACTORS COMPOUNDING
    // ========================================================================

    /**
     * Validates peak hour temporal weighting (+20% demand pressure):
     * Raw ratio 1.00 * Peak Factor 1.20 = Effective Ratio 1.20 -> S = 1.06x.
     */
    private static void testPeakHourSurgeCompounding() {
        System.out.println("\n--- Category 3: Environmental & Peak Compounding ---");
        MarketConditions cond = new MarketConditions(20, 20, true, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Peak Hour Effective Ratio", 1.20, b.getEffectiveDemandRatio(), 0.01, "Raw 1.0 * Peak 1.20");
        assertEqualDoubles("Peak Hour Multiplier", 1.06, b.getSurgeMultiplier(), 0.01, "Effective Ratio 1.20 -> S=1.06");
    }

    /**
     * Validates inclement weather multiplier (Heavy Rain = 1.30x):
     * Raw ratio 1.00 * Weather Factor 1.30 = Effective Ratio 1.30 -> S = 1.09x.
     */
    private static void testWeatherConditionsMultiplier() {
        MarketConditions cond = new MarketConditions(20, 20, false, WeatherCondition.HEAVY_RAIN);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Heavy Rain Effective Ratio", 1.30, b.getEffectiveDemandRatio(), 0.01, "Raw 1.0 * Rain 1.30");
        assertEqualDoubles("Heavy Rain Multiplier", 1.09, b.getSurgeMultiplier(), 0.01, "Effective Ratio 1.30 -> S=1.09");
    }

    /**
     * Validates simultaneous peak hour and severe storm compounding:
     * Raw ratio (30/15 = 2.0) * Peak (1.20) * Storm (1.50) = 3.60 Effective Ratio -> S = 2.37x.
     */
    private static void testCombinedPeakAndSevereStorm() {
        MarketConditions cond = new MarketConditions(30, 15, true, WeatherCondition.STORM);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Peak + Storm Effective Ratio", 3.60, b.getEffectiveDemandRatio(), 0.01, "2.0 * 1.20 * 1.50 = 3.60");
        assertEqualDoubles("Peak + Storm Multiplier", 2.37, b.getSurgeMultiplier(), 0.01, "Effective Ratio 3.60 -> S=2.37");
    }

    // ========================================================================
    // SUITE 4: EDGE CASES & OPERATIONAL BOUNDARIES
    // ========================================================================

    /**
     * Validates total driver supply exhaustion (availableDrivers == 0):
     * Verifies infinite demand ratio handling and fallback to 3.50x regulatory cap.
     */
    private static void testZeroAvailableDriversSupplyExhaustion() {
        System.out.println("\n--- Category 4: Edge Cases & Boundary Conditions ---");
        MarketConditions cond = new MarketConditions(25, 0, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertTrue("Zero Drivers Infinite Ratio", Double.isInfinite(b.getEffectiveDemandRatio()), "Infinite ratio handled");
        assertEqualDoubles("Zero Drivers Multiplier Capped", 3.50, b.getSurgeMultiplier(), 0.001, "Safety maximum cap applied");
        assertTrue("Zero Drivers Informative Diagnostic", b.getNotes().toLowerCase().contains("zero drivers"), b.getNotes());
    }

    /**
     * Validates zero booking requests (ratio = 0.00 => Multiplier = 1.00x).
     */
    private static void testZeroRideRequests() {
        MarketConditions cond = new MarketConditions(0, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Zero Requests Multiplier", 1.00, b.getSurgeMultiplier(), 0.001, "Ratio 0.0 -> Multiplier 1.00");
    }

    /**
     * Validates minimum supported distance boundary (0.1 km).
     */
    private static void testMinimumDistanceBoundary() {
        RideRequest req = new RideRequest("Origin", "Dest", FareValidator.MIN_DISTANCE_KM, 1.0, VehicleType.STANDARD);
        MarketConditions cond = new MarketConditions(10, 10, false, WeatherCondition.CLEAR);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertTrue("Minimum Distance Allowed", b.getRequest().getDistanceKm() == FareValidator.MIN_DISTANCE_KM, "Boundary 0.1km accepted");
    }

    /**
     * Validates maximum supported distance boundary (500.0 km).
     */
    private static void testMaximumDistanceBoundary() {
        RideRequest req = new RideRequest("Origin", "Dest", FareValidator.MAX_DISTANCE_KM, 300.0, VehicleType.STANDARD);
        MarketConditions cond = new MarketConditions(10, 10, false, WeatherCondition.CLEAR);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertTrue("Maximum Distance Allowed", b.getRequest().getDistanceKm() == FareValidator.MAX_DISTANCE_KM, "Boundary 500km accepted");
    }

    /**
     * Validates exact mathematical boundary inflection points (R_eff = 1.00, 1.50, 2.50)
     * to prove continuity across piecewise segment thresholds.
     */
    private static void testExactSurgeThresholdBoundaries() {
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);

        // Boundary 1: Ratio = 1.00 -> S = 1.00
        MarketConditions cond1 = new MarketConditions(20, 20, false, WeatherCondition.CLEAR);
        FareBreakdown b1 = engine.calculateFare(req, cond1);
        assertEqualDoubles("Boundary Ratio 1.00", 1.00, b1.getSurgeMultiplier(), 0.001, "S = 1.00");

        // Boundary 2: Ratio = 1.50 -> S = 1.00 + 0.30*(0.50) = 1.15
        MarketConditions cond2 = new MarketConditions(30, 20, false, WeatherCondition.CLEAR);
        FareBreakdown b2 = engine.calculateFare(req, cond2);
        assertEqualDoubles("Boundary Ratio 1.50", 1.15, b2.getSurgeMultiplier(), 0.001, "S = 1.15");

        // Boundary 3: Ratio = 2.50 -> S = 1.15 + 0.50*(1.00) = 1.65
        MarketConditions cond3 = new MarketConditions(50, 20, false, WeatherCondition.CLEAR);
        FareBreakdown b3 = engine.calculateFare(req, cond3);
        assertEqualDoubles("Boundary Ratio 2.50", 1.65, b3.getSurgeMultiplier(), 0.001, "S = 1.65");
    }

    // ========================================================================
    // SUITE 5: DEFENSIVE INPUT VALIDATION & EXCEPTION HANDLING
    // ========================================================================

    /**
     * Rejection test: empty pickup location.
     */
    private static void testValidationEmptyPickupLocation() {
        System.out.println("\n--- Category 5: Input Validation & Exception Handling ---");
        try {
            RideRequest req = new RideRequest("", "Airport", 10.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Empty Pickup", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Empty Pickup", true, "Caught expected: " + ex.getMessage());
        }
    }

    /**
     * Rejection test: whitespace-only destination.
     */
    private static void testValidationEmptyDestination() {
        try {
            RideRequest req = new RideRequest("Station", "   ", 10.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Empty Destination", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Empty Destination", true, "Caught expected: " + ex.getMessage());
        }
    }

    /**
     * Rejection test: identical pickup and destination addresses.
     */
    private static void testValidationIdenticalLocations() {
        try {
            RideRequest req = new RideRequest("Central Station", "Central Station", 10.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Identical Origin/Destination", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Identical Origin/Destination", true, "Caught expected: " + ex.getMessage());
        }
    }

    /**
     * Rejection test: negative distance value.
     */
    private static void testValidationNegativeDistance() {
        try {
            RideRequest req = new RideRequest("Station", "Airport", -5.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Negative Distance", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Distance", true, "Caught expected: " + ex.getMessage());
        }
    }

    /**
     * Rejection test: zero distance value.
     */
    private static void testValidationZeroDistance() {
        try {
            RideRequest req = new RideRequest("Station", "Airport", 0.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Zero Distance", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Zero Distance", true, "Caught expected: " + ex.getMessage());
        }
    }

    /**
     * Rejection test: negative trip duration.
     */
    private static void testValidationNegativeDuration() {
        try {
            RideRequest req = new RideRequest("Station", "Airport", 10.0, -15.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Negative Duration", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Duration", true, "Caught expected: " + ex.getMessage());
        }
    }

    /**
     * Rejection test: negative ride requests count.
     */
    private static void testValidationNegativeRequests() {
        try {
            MarketConditions cond = new MarketConditions(-5, 10, false, WeatherCondition.CLEAR);
            FareValidator.validateMarketConditions(cond);
            assertTrue("Reject Negative Requests", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Requests", true, "Caught expected: " + ex.getMessage());
        }
    }

    /**
     * Rejection test: negative available drivers count.
     */
    private static void testValidationNegativeDrivers() {
        try {
            MarketConditions cond = new MarketConditions(10, -3, false, WeatherCondition.CLEAR);
            FareValidator.validateMarketConditions(cond);
            assertTrue("Reject Negative Drivers", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Drivers", true, "Caught expected: " + ex.getMessage());
        }
    }

    // ========================================================================
    // SUITE 6: DETERMINISM & MATHEMATICAL CONSISTENCY
    // ========================================================================

    /**
     * Validates that the pricing algorithm produces 100% deterministic output
     * across 100 consecutive executions with identical inputs.
     */
    private static void testCalculationDeterminism() {
        System.out.println("\n--- Category 6: Determinism & Monotonicity ---");
        RideRequest req = new RideRequest("Downtown", "Uptown", 14.2, 28.5, VehicleType.STANDARD);
        MarketConditions cond = new MarketConditions(45, 18, true, WeatherCondition.RAIN);

        FareBreakdown first = engine.calculateFare(req, cond);
        boolean allIdentical = true;

        for (int i = 0; i < 100; i++) {
            FareBreakdown next = engine.calculateFare(req, cond);
            if (Double.compare(first.getFinalFare(), next.getFinalFare()) != 0 ||
                Double.compare(first.getSurgeMultiplier(), next.getSurgeMultiplier()) != 0) {
                allIdentical = false;
                break;
            }
        }

        assertTrue("Calculation Determinism (100 runs)", allIdentical, "Exact identical output on repeated runs");
    }

    /**
     * Mathematically proves that the surge multiplier function is strictly monotonic:
     * as ride requests increase from 10 to 120 with constant supply, the multiplier
     * never decreases: dS/dD >= 0.
     */
    private static void testMonotonicityOfSurgeMultiplier() {
        RideRequest req = new RideRequest("A", "B", 10.0, 20.0, VehicleType.STANDARD);
        double previousMultiplier = 1.0;
        boolean monotonic = true;

        for (int reqCount = 10; reqCount <= 120; reqCount += 5) {
            MarketConditions cond = new MarketConditions(reqCount, 20, false, WeatherCondition.CLEAR);
            FareBreakdown b = engine.calculateFare(req, cond);
            if (b.getSurgeMultiplier() < previousMultiplier) {
                monotonic = false;
                break;
            }
            previousMultiplier = b.getSurgeMultiplier();
        }

        assertTrue("Monotonicity Across Demand Growth", monotonic, "Surge multiplier never decreases as demand grows");
    }
}
