package com.ridefare;

import com.ridefare.engine.FareValidator;
import com.ridefare.engine.PricingEngine;
import com.ridefare.model.FareBreakdown;
import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;
import com.ridefare.model.VehicleType;
import com.ridefare.model.WeatherCondition;

/**
 * Automated test suite for RideFare Dynamic Surge Pricing Engine.
 * Executes unit tests, boundary tests, edge cases, validation tests,
 * and mathematical verification without requiring external test dependencies.
 */
public class RideFareTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;
    private static final PricingEngine engine = new PricingEngine();

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("              RIDEFARE AUTOMATED TEST SUITE EXECUTION (CP-044)                  ");
        System.out.println("================================================================================");

        long startTime = System.currentTimeMillis();

        // 1. Normal Baseline Fare Calculations
        testNormalFareCalculation();
        testVehicleTiersTariffRates();

        // 2. Supply and Demand Scenarios
        testLowDemandScenario();
        testBalancedMarketScenario();
        testMildSurgeScenario();
        testHighSurgeScenario();
        testExtremeSurgeScenario();
        testMaximumCapEnforcement();

        // 3. Environmental & Temporal Factors
        testPeakHourSurgeCompounding();
        testWeatherConditionsMultiplier();
        testCombinedPeakAndSevereStorm();

        // 4. Edge Cases & Boundary Conditions
        testZeroAvailableDriversSupplyExhaustion();
        testZeroRideRequests();
        testMinimumDistanceBoundary();
        testMaximumDistanceBoundary();
        testExactSurgeThresholdBoundaries();

        // 5. Input Validation & Error Handling
        testValidationEmptyPickupLocation();
        testValidationEmptyDestination();
        testValidationIdenticalLocations();
        testValidationNegativeDistance();
        testValidationZeroDistance();
        testValidationNegativeDuration();
        testValidationNegativeRequests();
        testValidationNegativeDrivers();

        // 6. Determinism & Mathematical Consistency
        testCalculationDeterminism();
        testMonotonicityOfSurgeMultiplier();

        long elapsed = System.currentTimeMillis() - startTime;

        // Print Summary Report
        System.out.println("\n================================================================================");
        System.out.println("                             TEST EXECUTION SUMMARY                             ");
        System.out.println("================================================================================");
        System.out.printf("Total Test Cases Executed: %d%n", totalTests);
        System.out.printf("Passed:                    %d (%.1f%%)%n", passedTests, (totalTests > 0 ? (passedTests * 100.0 / totalTests) : 0));
        System.out.printf("Failed:                    %d%n", failedTests);
        System.out.printf("Execution Time:            %d ms%n", elapsed);
        System.out.println("================================================================================");

        if (failedTests == 0) {
            System.out.println("[SUCCESS] ALL TEST CASES PASSED WITH 100% INTEGRITY.");
        } else {
            System.err.println("[FAILURE] SOME TEST CASES FAILED. REVIEW SYSTEM LOGS.");
            System.exit(1);
        }
    }

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

    private static void assertEqualDoubles(String testName, double expected, double actual, double delta, String details) {
        boolean match = Math.abs(expected - actual) <= delta;
        assertTrue(testName, match, String.format("Expected=%.2f, Actual=%.2f | %s", expected, actual, details));
    }

    // --- TEST IMPLEMENTATIONS ---

    private static void testNormalFareCalculation() {
        System.out.println("\n--- Category 1: Baseline Fare Calculations ---");
        // Standard sedan: Base $3.50, Per Km $1.25, Per Min $0.25
        // Distance: 10 km -> 10 * 1.25 = $12.50
        // Duration: 20 min -> 20 * 0.25 = $5.00
        // Expected Normal Fare: 3.50 + 12.50 + 5.00 = $21.00
        RideRequest req = new RideRequest("Point A", "Point B", 10.0, 20.0, VehicleType.STANDARD);
        MarketConditions cond = new MarketConditions(10, 20, false, WeatherCondition.CLEAR);

        FareBreakdown b = engine.calculateFare(req, cond);
        assertEqualDoubles("Standard Fare Un-surged", 21.00, b.getNormalFare(), 0.01, "Base $3.50 + Dist $12.50 + Time $5.00");
        assertEqualDoubles("Standard Multiplier 1.00x", 1.00, b.getSurgeMultiplier(), 0.001, "Normal demand ratio 0.50");
        assertEqualDoubles("Final Fare Equals Normal Fare", 21.00, b.getFinalFare(), 0.01, "No surge delta added");
    }

    private static void testVehicleTiersTariffRates() {
        RideRequest reqEco = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.ECONOMY);
        RideRequest reqPrem = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.PREMIUM);
        MarketConditions cond = new MarketConditions(10, 10, false, WeatherCondition.CLEAR);

        // Economy: 2.50 + (10 * 0.90) + (20 * 0.15) = 2.50 + 9.00 + 3.00 = $14.50
        FareBreakdown bEco = engine.calculateFare(reqEco, cond);
        assertEqualDoubles("Economy Tier Tariff", 14.50, bEco.getNormalFare(), 0.01, "Economy 10km, 20min");

        // Premium: 5.50 + (10 * 2.10) + (20 * 0.40) = 5.50 + 21.00 + 8.00 = $34.50
        FareBreakdown bPrem = engine.calculateFare(reqPrem, cond);
        assertEqualDoubles("Premium Tier Tariff", 34.50, bPrem.getNormalFare(), 0.01, "Premium 10km, 20min");
    }

    private static void testLowDemandScenario() {
        System.out.println("\n--- Category 2: Supply and Demand Dynamics ---");
        // 10 requests, 40 drivers -> Ratio = 0.25 (<= 1.0) -> Multiplier = 1.00x
        MarketConditions cond = new MarketConditions(10, 40, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Low Demand Multiplier", 1.00, b.getSurgeMultiplier(), 0.01, "Ratio 0.25 <= 1.0");
        assertTrue("Low Demand Level Description", b.getDemandLevel().contains("Normal Demand"), b.getDemandLevel());
    }

    private static void testBalancedMarketScenario() {
        // 20 requests, 20 drivers -> Ratio = 1.00 (<= 1.0) -> Multiplier = 1.00x
        MarketConditions cond = new MarketConditions(20, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Balanced Market Multiplier", 1.00, b.getSurgeMultiplier(), 0.01, "Ratio 1.00 <= 1.0");
    }

    private static void testMildSurgeScenario() {
        // 26 requests, 20 drivers -> Ratio = 1.30 (1.0 < Ratio <= 1.5)
        // Multiplier = 1.00 + 0.30 * (1.30 - 1.0) = 1.00 + 0.09 = 1.09x
        MarketConditions cond = new MarketConditions(26, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Mild Surge Multiplier", 1.09, b.getSurgeMultiplier(), 0.01, "Ratio 1.30 -> S=1.09");
        assertTrue("Mild Surge Label", b.getDemandLevel().contains("Mild"), b.getDemandLevel());
    }

    private static void testHighSurgeScenario() {
        // 40 requests, 20 drivers -> Ratio = 2.00 (1.5 < Ratio <= 2.5)
        // Multiplier = 1.15 + 0.50 * (2.00 - 1.5) = 1.15 + 0.25 = 1.40x
        MarketConditions cond = new MarketConditions(40, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("High Surge Multiplier", 1.40, b.getSurgeMultiplier(), 0.01, "Ratio 2.00 -> S=1.40");
        assertTrue("High Surge Label", b.getDemandLevel().contains("High"), b.getDemandLevel());
    }

    private static void testExtremeSurgeScenario() {
        // 60 requests, 20 drivers -> Ratio = 3.00 (> 2.5)
        // Multiplier = 1.65 + 0.65 * (3.00 - 2.5) = 1.65 + 0.325 = 1.975 -> 1.98x
        MarketConditions cond = new MarketConditions(60, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Extreme Surge Multiplier", 1.98, b.getSurgeMultiplier(), 0.01, "Ratio 3.00 -> S=1.98");
        assertTrue("Extreme Surge Label", b.getDemandLevel().contains("Extreme"), b.getDemandLevel());
    }

    private static void testMaximumCapEnforcement() {
        // 200 requests, 10 drivers -> Ratio = 20.0
        // Multiplier calculated would exceed 3.50 -> Capped strictly at 3.50x
        MarketConditions cond = new MarketConditions(200, 10, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Maximum Cap Multiplier", 3.50, b.getSurgeMultiplier(), 0.001, "Strict cap at 3.50x");
        assertTrue("Maximum Cap Enforced Label", b.getDemandLevel().contains("Maximum Surge Cap"), b.getDemandLevel());
    }

    private static void testPeakHourSurgeCompounding() {
        System.out.println("\n--- Category 3: Environmental & Peak Compounding ---");
        // 20 requests, 20 drivers, Peak Hour active (1.20x) -> Effective ratio = 1.20
        // Multiplier = 1.00 + 0.30 * (1.20 - 1.0) = 1.06x
        MarketConditions cond = new MarketConditions(20, 20, true, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Peak Hour Effective Ratio", 1.20, b.getEffectiveDemandRatio(), 0.01, "Raw 1.0 * Peak 1.20");
        assertEqualDoubles("Peak Hour Multiplier", 1.06, b.getSurgeMultiplier(), 0.01, "Effective Ratio 1.20 -> S=1.06");
    }

    private static void testWeatherConditionsMultiplier() {
        // 20 requests, 20 drivers, Heavy Rain (1.30x) -> Effective ratio = 1.30
        // Multiplier = 1.00 + 0.30 * (1.30 - 1.0) = 1.09x
        MarketConditions cond = new MarketConditions(20, 20, false, WeatherCondition.HEAVY_RAIN);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Heavy Rain Effective Ratio", 1.30, b.getEffectiveDemandRatio(), 0.01, "Raw 1.0 * Rain 1.30");
        assertEqualDoubles("Heavy Rain Multiplier", 1.09, b.getSurgeMultiplier(), 0.01, "Effective Ratio 1.30 -> S=1.09");
    }

    private static void testCombinedPeakAndSevereStorm() {
        // 30 requests, 15 drivers (Raw = 2.0), Peak (1.20), Storm (1.50)
        // Effective ratio = 2.0 * 1.20 * 1.50 = 3.60
        // Multiplier = 1.65 + 0.65 * (3.60 - 2.5) = 1.65 + 0.715 = 2.365 -> 2.37x
        MarketConditions cond = new MarketConditions(30, 15, true, WeatherCondition.STORM);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Peak + Storm Effective Ratio", 3.60, b.getEffectiveDemandRatio(), 0.01, "2.0 * 1.20 * 1.50 = 3.60");
        assertEqualDoubles("Peak + Storm Multiplier", 2.37, b.getSurgeMultiplier(), 0.01, "Effective Ratio 3.60 -> S=2.37");
    }

    private static void testZeroAvailableDriversSupplyExhaustion() {
        System.out.println("\n--- Category 4: Edge Cases & Boundary Conditions ---");
        // Zero available drivers -> Infinite demand ratio -> Safety max cap 3.50x
        MarketConditions cond = new MarketConditions(25, 0, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertTrue("Zero Drivers Infinite Ratio", Double.isInfinite(b.getEffectiveDemandRatio()), "Infinite ratio handled");
        assertEqualDoubles("Zero Drivers Multiplier Capped", 3.50, b.getSurgeMultiplier(), 0.001, "Safety maximum cap applied");
        assertTrue("Zero Drivers Informative Diagnostic", b.getNotes().toLowerCase().contains("zero drivers"), b.getNotes());
    }

    private static void testZeroRideRequests() {
        // Zero ride requests, 20 drivers -> Ratio = 0.00 -> Multiplier = 1.00x
        MarketConditions cond = new MarketConditions(0, 20, false, WeatherCondition.CLEAR);
        RideRequest req = new RideRequest("Origin", "Dest", 10.0, 20.0, VehicleType.STANDARD);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertEqualDoubles("Zero Requests Multiplier", 1.00, b.getSurgeMultiplier(), 0.001, "Ratio 0.0 -> Multiplier 1.00");
    }

    private static void testMinimumDistanceBoundary() {
        // Minimum distance: 0.1 km
        RideRequest req = new RideRequest("Origin", "Dest", FareValidator.MIN_DISTANCE_KM, 1.0, VehicleType.STANDARD);
        MarketConditions cond = new MarketConditions(10, 10, false, WeatherCondition.CLEAR);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertTrue("Minimum Distance Allowed", b.getRequest().getDistanceKm() == FareValidator.MIN_DISTANCE_KM, "Boundary 0.1km accepted");
    }

    private static void testMaximumDistanceBoundary() {
        // Maximum distance: 500.0 km
        RideRequest req = new RideRequest("Origin", "Dest", FareValidator.MAX_DISTANCE_KM, 300.0, VehicleType.STANDARD);
        MarketConditions cond = new MarketConditions(10, 10, false, WeatherCondition.CLEAR);
        FareBreakdown b = engine.calculateFare(req, cond);

        assertTrue("Maximum Distance Allowed", b.getRequest().getDistanceKm() == FareValidator.MAX_DISTANCE_KM, "Boundary 500km accepted");
    }

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

    private static void testValidationEmptyDestination() {
        try {
            RideRequest req = new RideRequest("Station", "   ", 10.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Empty Destination", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Empty Destination", true, "Caught expected: " + ex.getMessage());
        }
    }

    private static void testValidationIdenticalLocations() {
        try {
            RideRequest req = new RideRequest("Central Station", "Central Station", 10.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Identical Origin/Destination", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Identical Origin/Destination", true, "Caught expected: " + ex.getMessage());
        }
    }

    private static void testValidationNegativeDistance() {
        try {
            RideRequest req = new RideRequest("Station", "Airport", -5.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Negative Distance", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Distance", true, "Caught expected: " + ex.getMessage());
        }
    }

    private static void testValidationZeroDistance() {
        try {
            RideRequest req = new RideRequest("Station", "Airport", 0.0, 20.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Zero Distance", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Zero Distance", true, "Caught expected: " + ex.getMessage());
        }
    }

    private static void testValidationNegativeDuration() {
        try {
            RideRequest req = new RideRequest("Station", "Airport", 10.0, -15.0, VehicleType.STANDARD);
            FareValidator.validateRideRequest(req);
            assertTrue("Reject Negative Duration", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Duration", true, "Caught expected: " + ex.getMessage());
        }
    }

    private static void testValidationNegativeRequests() {
        try {
            MarketConditions cond = new MarketConditions(-5, 10, false, WeatherCondition.CLEAR);
            FareValidator.validateMarketConditions(cond);
            assertTrue("Reject Negative Requests", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Requests", true, "Caught expected: " + ex.getMessage());
        }
    }

    private static void testValidationNegativeDrivers() {
        try {
            MarketConditions cond = new MarketConditions(10, -3, false, WeatherCondition.CLEAR);
            FareValidator.validateMarketConditions(cond);
            assertTrue("Reject Negative Drivers", false, "Should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue("Reject Negative Drivers", true, "Caught expected: " + ex.getMessage());
        }
    }

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
