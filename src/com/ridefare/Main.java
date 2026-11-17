package com.ridefare;

import com.ridefare.ui.CommandLineUI;

/**
 * ============================================================================
 * RideFare Application Entry Point: Main.java
 * ============================================================================
 * 
 * Project ID   : CP-044
 * Project Title: RideFare: Dynamic Surge Pricing Engine for Ride-Hailing
 * Domain       : Travel, Transport and Logistics (Intelligent Systems)
 * Currency     : Indian Rupees (INR / ₹)
 * 
 * PURPOSE:
 * This is the starting point (entry point) of the entire RideFare application.
 * When you run the program, the Java Virtual Machine (JVM) starts here in the
 * main() method.
 * 
 * HOW IT WORKS:
 * 1. The user runs the program from the terminal/command prompt:
 *      java -jar RideFare.jar
 * 2. If the user passes "--help" or "-h", it prints friendly usage instructions.
 * 3. Otherwise, it starts the interactive Command Line Interface (CommandLineUI),
 *    which presents a clean, numbered menu for calculating fares, comparing
 *    vehicle categories, and running simulation scenarios.
 * ============================================================================
 */
public class Main {

    /**
     * The main method is where program execution begins.
     *
     * @param args Command-line arguments (e.g. "--help" to see usage instructions)
     */
    public static void main(String[] args) {
        // Check if the user asked for help instructions
        for (String arg : args) {
            if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printUsage();
                return; // Stop here after printing help
            }
        }

        // Start the interactive terminal menu
        CommandLineUI cli = new CommandLineUI();
        cli.start();
    }

    /**
     * Prints simple, beginner-friendly help instructions to the console.
     */
    private static void printUsage() {
        System.out.println("================================================================================");
        System.out.println("            RIDEFARE: DYNAMIC SURGE PRICING ENGINE (PROJECT CP-044)             ");
        System.out.println("                         Interactive CLI Mode (INR / ₹)                         ");
        System.out.println("================================================================================");
        System.out.println("How to run:");
        System.out.println("  java -jar RideFare.jar          Start the interactive menu");
        System.out.println("  java -jar RideFare.jar --help   Show this help message");
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
