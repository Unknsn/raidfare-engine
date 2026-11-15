package com.ridefare.ui;

import com.ridefare.engine.FareValidator;
import com.ridefare.engine.PricingEngine;
import com.ridefare.model.FareBreakdown;
import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;
import com.ridefare.model.VehicleType;
import com.ridefare.model.WeatherCondition;

import java.util.Scanner;

/**
 * Command-line interactive user interface for RideFare.
 */
public class CommandLineUI {

    private final PricingEngine engine;
    private final Scanner scanner;

    public CommandLineUI() {
        this.engine = new PricingEngine();
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        printWelcomeBanner();

        while (running) {
            printMainMenu();
            String choice = promptString("Enter your choice (1-5): ");

            switch (choice.trim()) {
                case "1":
                    handleInteractiveBooking();
                    break;
                case "2":
                    handleTierComparison();
                    break;
                case "3":
                    handlePresetScenarios();
                    break;
                case "4":
                    displayAlgorithmAndRates();
                    break;
                case "5":
                    System.out.println("\nExiting RideFare Engine. Thank you for using the system!");
                    running = false;
                    break;
                default:
                    System.out.println("[ERROR] Invalid choice. Please enter a number between 1 and 5.\n");
            }
        }
    }

    private void printWelcomeBanner() {
        System.out.println("================================================================================");
        System.out.println("            RIDEFARE: DYNAMIC SURGE PRICING ENGINE (PROJECT CP-044)             ");
        System.out.println("               Domain: Travel, Transport and Logistics                         ");
        System.out.println("================================================================================");
    }

    private void printMainMenu() {
        System.out.println("\n----------------------------- MAIN NAVIGATION MENU -----------------------------");
        System.out.println("  [1] Calculate Dynamic Ride Fare (Interactive Input)");
        System.out.println("  [2] Compare Fares Across Vehicle Tiers (Economy vs Standard vs Premium)");
        System.out.println("  [3] Run Benchmark Simulation Scenarios (Low, Normal, Peak, Extreme, Zero-Driver)");
        System.out.println("  [4] View Pricing Algorithm Formulas & Vehicle Rate Cards");
        System.out.println("  [5] Exit Application");
        System.out.println("--------------------------------------------------------------------------------");
    }

    private void handleInteractiveBooking() {
        System.out.println("\n--- INTERACTIVE FARE CALCULATION ---");

        try {
            String pickup = promptString("Enter pickup location: ");
            String destination = promptString("Enter destination: ");
            double distance = promptDouble("Enter travel distance (in kilometers, e.g. 14.5): ");
            double duration = promptDouble("Enter estimated journey duration (in minutes, e.g. 25): ");

            System.out.println("\nSelect Vehicle Type:");
            System.out.println("  1. Economy (Mini)   - Base: $2.50 | $0.90/km | $0.15/min");
            System.out.println("  2. Standard (Sedan) - Base: $3.50 | $1.25/km | $0.25/min");
            System.out.println("  3. Premium (SUV)    - Base: $5.50 | $2.10/km | $0.40/min");
            int vehicleChoice = promptInt("Select vehicle (1-3, default 2): ", 2);
            VehicleType vehicleType = mapVehicleChoice(vehicleChoice);

            System.out.println("\n--- Real-Time Market Conditions ---");
            int requests = promptInt("Enter current ride requests in zone: ", 10);
            int drivers = promptInt("Enter available drivers in zone: ", 10);
            boolean peak = promptBoolean("Is it currently peak hour? (y/n): ");

            System.out.println("\nSelect Weather Condition:");
            System.out.println("  1. Clear / Normal (1.00x factor)");
            System.out.println("  2. Moderate Rain  (1.15x factor)");
            System.out.println("  3. Heavy Rain     (1.30x factor)");
            System.out.println("  4. Severe Storm   (1.50x factor)");
            int weatherChoice = promptInt("Select weather (1-4, default 1): ", 1);
            WeatherCondition weather = mapWeatherChoice(weatherChoice);

            RideRequest request = new RideRequest(pickup, destination, distance, duration, vehicleType);
            MarketConditions conditions = new MarketConditions(requests, drivers, peak, weather);

            FareBreakdown breakdown = engine.calculateFare(request, conditions);
            displayFareBreakdown(breakdown);

        } catch (IllegalArgumentException ex) {
            System.out.println("\n[VALIDATION FAILED] " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("\n[UNEXPECTED ERROR] " + ex.getMessage());
        }
    }

    private void handleTierComparison() {
        System.out.println("\n--- MULTI-TIER VEHICLE COMPARISON ---");

        try {
            String pickup = promptString("Enter pickup location: ");
            String destination = promptString("Enter destination: ");
            double distance = promptDouble("Enter travel distance (km): ");
            double duration = promptDouble("Enter estimated journey duration (minutes): ");

            int requests = promptInt("Current ride requests: ", 25);
            int drivers = promptInt("Available drivers: ", 15);
            boolean peak = promptBoolean("Is peak hour? (y/n): ");
            WeatherCondition weather = WeatherCondition.CLEAR;

            MarketConditions conditions = new MarketConditions(requests, drivers, peak, weather);

            System.out.println("\n================================================================================");
            System.out.println(String.format("FARE COMPARISON: '%s' to '%s' (%.2f km, %.1f mins)",
                    pickup, destination, distance, duration));
            System.out.println(String.format("Market: Requests=%d, Drivers=%d, Peak=%b, Weather=%s",
                    requests, drivers, peak, weather.getDisplayName()));
            System.out.println("================================================================================");
            System.out.printf("%-18s | %-12s | %-8s | %-12s | %-12s%n",
                    "Vehicle Tier", "Normal Fare", "Surge", "Final Fare", "Difference");
            System.out.println("--------------------------------------------------------------------------------");

            for (VehicleType type : VehicleType.values()) {
                RideRequest req = new RideRequest(pickup, destination, distance, duration, type);
                FareBreakdown b = engine.calculateFare(req, conditions);
                System.out.printf("%-18s | $ %10.2f | %6.2fx | $ %10.2f | +$ %9.2f%n",
                        type.getDisplayName(), b.getNormalFare(), b.getSurgeMultiplier(),
                        b.getFinalFare(), b.getSurgeDelta());
            }
            System.out.println("================================================================================");

        } catch (IllegalArgumentException ex) {
            System.out.println("\n[VALIDATION FAILED] " + ex.getMessage());
        }
    }

    private void handlePresetScenarios() {
        System.out.println("\n================================================================================");
        System.out.println("                     BENCHMARK SIMULATION SCENARIOS                             ");
        System.out.println("================================================================================");

        String pickup = "Central Station";
        String dest = "Tech Park";
        double dist = 12.0;
        double time = 24.0;
        VehicleType type = VehicleType.STANDARD;

        // Scenario 1: Low Demand / Abundant Supply
        runScenario("Scenario 1: Low Demand (Abundant Drivers)",
                pickup, dest, dist, time, type,
                new MarketConditions(15, 30, false, WeatherCondition.CLEAR));

        // Scenario 2: Normal Balanced Market
        runScenario("Scenario 2: Normal Balanced Market",
                pickup, dest, dist, time, type,
                new MarketConditions(25, 25, false, WeatherCondition.CLEAR));

        // Scenario 3: Evening Peak Commute
        runScenario("Scenario 3: Evening Peak Commute (Peak Factor Active)",
                pickup, dest, dist, time, type,
                new MarketConditions(60, 30, true, WeatherCondition.CLEAR));

        // Scenario 4: Monsoon Downpour (Inclement Weather Surge)
        runScenario("Scenario 4: Heavy Rain Weather Surge",
                pickup, dest, dist, time, type,
                new MarketConditions(70, 35, false, WeatherCondition.HEAVY_RAIN));

        // Scenario 5: Severe Friday Night Storm
        runScenario("Scenario 5: Severe Storm + Peak Demand (High Imbalance)",
                pickup, dest, dist, time, type,
                new MarketConditions(90, 20, true, WeatherCondition.STORM));

        // Scenario 6: Zero Available Drivers (Supply Exhaustion)
        runScenario("Scenario 6: Total Supply Exhaustion (Zero Drivers Available)",
                pickup, dest, dist, time, type,
                new MarketConditions(40, 0, false, WeatherCondition.CLEAR));
    }

    private void runScenario(String title, String pickup, String dest, double dist, double time,
                            VehicleType type, MarketConditions conditions) {
        System.out.println("\n>>> " + title);
        RideRequest req = new RideRequest(pickup, dest, dist, time, type);
        FareBreakdown b = engine.calculateFare(req, conditions);
        System.out.printf("    Conditions: Requests=%d, Drivers=%d, Peak=%b, Weather=%s%n",
                conditions.getRideRequests(), conditions.getAvailableDrivers(),
                conditions.isPeakHour(), conditions.getWeatherCondition().getDisplayName());
        System.out.printf("    Effective Ratio: %.2f | Surge Multiplier: %.2fx | Level: %s%n",
                b.getEffectiveDemandRatio(), b.getSurgeMultiplier(), b.getDemandLevel());
        System.out.printf("    Normal Fare: $%.2f | Final Fare: $%.2f (Delta: +$%.2f)%n",
                b.getNormalFare(), b.getFinalFare(), b.getSurgeDelta());
        System.out.printf("    Notes: %s%n", b.getNotes());
    }

    private void displayAlgorithmAndRates() {
        System.out.println("\n================================================================================");
        System.out.println("                 RIDEFARE PRICING ALGORITHM & RATE CARDS                        ");
        System.out.println("================================================================================");
        System.out.println("\n1. VEHICLE RATE CARDS (BASE TARIFFS):");
        System.out.println("   +--------------------+-----------+------------+--------------+");
        System.out.println("   | Vehicle Category   | Base Fare | Per Km Rate| Per Min Rate |");
        System.out.println("   +--------------------+-----------+------------+--------------+");
        System.out.println("   | Economy (Mini)     |   $2.50   |   $0.90    |    $0.15     |");
        System.out.println("   | Standard (Sedan)   |   $3.50   |   $1.25    |    $0.25     |");
        System.out.println("   | Premium (SUV)      |   $5.50   |   $2.10    |    $0.40     |");
        System.out.println("   +--------------------+-----------+------------+--------------+");

        System.out.println("\n2. PRICING FORMULA:");
        System.out.println("   Normal Fare = BaseFare + (Distance * RatePerKm) + (Duration * RatePerMin)");
        System.out.println("   Effective Demand Ratio = (Requests / Drivers) * PeakFactor * WeatherFactor");
        System.out.println("   Final Dynamic Fare  = Normal Fare * SurgeMultiplier");

        System.out.println("\n3. MULTIPLIER THRESHOLDS & BOUNDARIES:");
        System.out.println("   - Effective Ratio <= 1.0 : Multiplier = 1.00x (Standard Fare)");
        System.out.println("   - 1.0 < Ratio <= 1.5     : Multiplier = 1.00 + 0.30 * (Ratio - 1.0)");
        System.out.println("   - 1.5 < Ratio <= 2.5     : Multiplier = 1.15 + 0.50 * (Ratio - 1.5)");
        System.out.println("   - Ratio > 2.5            : Multiplier = 1.65 + 0.65 * (Ratio - 2.5)");
        System.out.println("   - Supply Exhaustion (0 Drivers): Multiplier = 3.50x (Regulatory Max Cap)");
        System.out.println("   - Minimum Multiplier Cap : 1.00x");
        System.out.println("   - Maximum Multiplier Cap : 3.50x (Strict Anti-Gouging Ceiling)");

        System.out.println("\n4. ENVIRONMENTAL & PEAK FACTORS:");
        System.out.println("   - Peak Hours: 1.20x demand weighting");
        System.out.println("   - Clear Weather: 1.00x | Rain: 1.15x | Heavy Rain: 1.30x | Storm: 1.50x");
        System.out.println("================================================================================\n");
    }

    public void displayFareBreakdown(FareBreakdown b) {
        RideRequest req = b.getRequest();
        MarketConditions cond = b.getConditions();

        System.out.println("\n================================================================================");
        System.out.println("                       ITEMIZED FARE ESTIMATE RECEIPT                           ");
        System.out.println("================================================================================");
        System.out.printf("Pickup Location:        %s%n", req.getPickupLocation());
        System.out.printf("Destination:            %s%n", req.getDestination());
        System.out.printf("Distance & Duration:    %.2f km | %.1f minutes%n", req.getDistanceKm(), req.getDurationMinutes());
        System.out.printf("Vehicle Category:       %s%n", req.getVehicleType().getDisplayName());

        System.out.println("\n----------------------------- MARKET DIAGNOSTICS -------------------------------");
        System.out.printf("Active Requests:        %d%n", cond.getRideRequests());
        System.out.printf("Available Drivers:      %d%n", cond.getAvailableDrivers());
        if (cond.getAvailableDrivers() > 0) {
            System.out.printf("Raw Demand-Supply Ratio:%.2f%n", cond.getRawDemandRatio());
        } else {
            System.out.println("Raw Demand-Supply Ratio:INFINITE (Supply Exhaustion)");
        }
        System.out.printf("Peak Hour Adjustment:   %s%n", cond.isPeakHour() ? "Active (1.20x)" : "Inactive (1.00x)");
        System.out.printf("Weather Condition:      %s (%.2fx impact)%n",
                cond.getWeatherCondition().getDisplayName(), cond.getWeatherCondition().getImpactMultiplier());
        System.out.printf("Effective Demand Ratio: %.2f%n", b.getEffectiveDemandRatio());
        System.out.printf("Market Surge Level:     %s%n", b.getDemandLevel());

        System.out.println("\n----------------------------- FINANCIAL BREAKDOWN ------------------------------");
        System.out.printf("(+) Base Fare:                    $ %8.2f%n", b.getBaseFare());
        System.out.printf("(+) Distance Charge (%.2f km):    $ %8.2f%n", req.getDistanceKm(), b.getDistanceCharge());
        System.out.printf("(+) Time Charge (%.1f mins):      $ %8.2f%n", req.getDurationMinutes(), b.getTimeCharge());
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("Standard Normal Fare:             $ %8.2f%n", b.getNormalFare());
        System.out.printf("Surge Pricing Multiplier:           %6.2fx%n", b.getSurgeMultiplier());
        if (b.hasSurge()) {
            System.out.printf("Dynamic Surge Additional Charge: +$ %8.2f%n", b.getSurgeDelta());
        } else {
            System.out.println("Dynamic Surge Additional Charge:  $     0.00 (Standard Pricing Applies)");
        }
        System.out.println("================================================================================");
        System.out.printf("TOTAL PAYABLE FARE:               $ %8.2f%n", b.getFinalFare());
        System.out.println("================================================================================");
        System.out.printf("System Notice: %s%n", b.getNotes());
        System.out.println("================================================================================\n");
    }

    private String promptString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private double promptDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException ex) {
                System.out.println("[ERROR] Invalid decimal number. Please try again.");
            }
        }
    }

    private int promptInt(String prompt, int defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                return defaultValue;
            }
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException ex) {
                System.out.println("[ERROR] Invalid integer. Please try again.");
            }
        }
    }

    private boolean promptBoolean(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim().toLowerCase();
        return input.startsWith("y") || input.equals("true") || input.equals("1");
    }

    private VehicleType mapVehicleChoice(int choice) {
        switch (choice) {
            case 1:
                return VehicleType.ECONOMY;
            case 3:
                return VehicleType.PREMIUM;
            case 2:
            default:
                return VehicleType.STANDARD;
        }
    }

    private WeatherCondition mapWeatherChoice(int choice) {
        switch (choice) {
            case 2:
                return WeatherCondition.RAIN;
            case 3:
                return WeatherCondition.HEAVY_RAIN;
            case 4:
                return WeatherCondition.STORM;
            case 1:
            default:
                return WeatherCondition.CLEAR;
        }
    }
}
