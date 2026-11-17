package com.ridefare;

import com.ridefare.ui.CommandLineUI;

/**
 * RideFare Application Entry Point (Main.java)
 * 
 * Project ID   : CP-044
 * Project Title: RideFare: Dynamic Surge Pricing Engine for Ride-Hailing
 * Currency     : Indian Rupees (INR / ₹)
 * 
 * How to Run directly (No JAR or build tool needed!):
 *   java src/com/ridefare/Main.java          Start the interactive menu
 *   java src/com/ridefare/Main.java --test   Run automated test suite (41 tests)
 *   java src/com/ridefare/Main.java --help   Show usage instructions
 */
public class Main {

    public static void main(String[] args) {
        // Ensure UTF-8 console output for Indian Rupee symbol (₹)
        try {
            System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
        } catch (Exception ignored) {
        }

        // Check command-line arguments
        for (String arg : args) {
            if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printUsage();
                return;
            }
            if ("--test".equalsIgnoreCase(arg) || "-t".equalsIgnoreCase(arg)) {
                RideFareTest.main(new String[0]);
                return;
            }
        }

        // Launch the interactive terminal menu
        CommandLineUI cli = new CommandLineUI();
        cli.start();
    }

    /**
     * Prints simple usage instructions and features.
     */
    private static void printUsage() {
        System.out.println("================================================================================");
        System.out.println("            RIDEFARE: DYNAMIC SURGE PRICING ENGINE (PROJECT CP-044)             ");
        System.out.println("                         Interactive CLI Mode (INR / ₹)                         ");
        System.out.println("================================================================================");
        System.out.println("How to run directly (no JAR needed):");
        System.out.println("  java src/com/ridefare/Main.java          Start the interactive menu");
        System.out.println("  java src/com/ridefare/Main.java --test   Run the automated test suite (41 tests)");
        System.out.println("  java src/com/ridefare/Main.java --help   Show this help message");
        System.out.println();
        System.out.println("Main Features:");
        System.out.println("  [1] Interactive Fare Calculator (Pickup, Destination, Km, Minutes, Market)");
        System.out.println("  [2] Vehicle Tier Comparison (Economy vs Standard vs Premium in ₹)");
        System.out.println("  [3] Benchmark Simulation Scenarios (Low Demand, Peak, Storm, Zero Drivers)");
        System.out.println("  [4] Rate Cards & Pricing Formula Explanation");
        System.out.println("  [5] Exit");
        System.out.println("================================================================================");
    }
}
