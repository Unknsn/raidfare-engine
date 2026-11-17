package com.ridefare;

import com.ridefare.ui.CommandLineUI;
import com.ridefare.ui.GraphicalUI;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * ============================================================================
 * Application Entry Point: Main.java
 * ============================================================================
 * 
 * Project ID   : CP-044
 * Project Title: RideFare: Dynamic Surge Pricing Engine for Ride-Hailing
 * Domain       : Travel, Transport and Logistics (Intelligent Systems)
 * 
 * ROLE AND PURPOSE:
 * This class serves as the single unified launcher for the entire RideFare
 * application. It inspects command-line arguments and runtime host capabilities
 * to intelligently determine whether to start the graphical desktop interface
 * (Java Swing) or the interactive console interface (CLI).
 * 
 * EXECUTION FLOW:
 * 1. Checks command-line flags (--gui, --cli, --help).
 * 2. If no explicit mode flag is supplied, it performs headless environment detection
 *    via GraphicsEnvironment.isHeadless().
 *    - In headless environments (e.g., remote SSH terminals, cloud containers, CI/CD),
 *      it cleanly boots the Terminal CLI to prevent HeadlessException crashes.
 *    - In desktop environments, it launches the Java Swing GUI on the Swing Event
 *      Dispatch Thread (EDT).
 * 3. Includes fallback logic: if GUI initialization encounters an unexpected display
 *    error, it gracefully falls back to CLI mode rather than crashing.
 * ============================================================================
 */
public class Main {

    /**
     * Primary entry point invoked by the Java Virtual Machine.
     *
     * @param args Command-line arguments passed at execution time:
     *             --gui or -g : Forces the desktop Java Swing interface.
     *             --cli or -c : Forces the interactive console terminal menu.
     *             --help or -h: Prints command syntax and options to stdout.
     */
    public static void main(String[] args) {
        boolean forceCli = false;
        boolean forceGui = false;

        // Step 1: Parse user-provided command-line arguments
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                forceCli = true;
            } else if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                forceGui = true;
            } else if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printUsage();
                return; // Terminate immediately after displaying help instructions
            }
        }

        // Step 2: Route execution to the chosen or auto-detected mode
        if (forceCli) {
            // User explicitly requested terminal mode
            launchCli();
        } else if (forceGui) {
            // User explicitly requested graphical mode
            launchGui();
        } else {
            // Intelligent automatic mode selection:
            // Check if the current JVM runtime has access to a display, keyboard, and mouse.
            // If headless (e.g. server environment), fallback to CLI; otherwise launch modern GUI.
            if (GraphicsEnvironment.isHeadless()) {
                System.out.println("[INFO] Headless console environment detected. Launching CLI mode...");
                launchCli();
            } else {
                launchGui();
            }
        }
    }

    /**
     * Initializes and renders the Java Swing Graphical User Interface.
     * 
     * Concurrency & Thread-Safety:
     * Swing GUI components are not thread-safe. Java Swing requires all UI
     * construction, layout updates, and rendering to occur strictly on the
     * Swing Event Dispatch Thread (EDT). SwingUtilities.invokeLater() schedules
     * this task asynchronously on the EDT queue.
     */
    private static void launchGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Instantiate the top-level JFrame and display it
                GraphicalUI gui = new GraphicalUI();
                gui.setVisible(true);
            } catch (Exception ex) {
                // Defensive recovery: If graphics display subsystem fails, fallback to CLI
                System.err.println("[WARN] Failed to initialize GUI: " + ex.getMessage());
                System.out.println("[INFO] Falling back to Command Line Interface...");
                launchCli();
            }
        });
    }

    /**
     * Initializes and starts the interactive Command Line Interface.
     * Runs synchronously on the calling main thread.
     */
    private static void launchCli() {
        CommandLineUI cli = new CommandLineUI();
        cli.start(); // Enters the interactive menu loop
    }

    /**
     * Displays command syntax, supported flags, and usage instructions in the console.
     */
    private static void printUsage() {
        System.out.println("================================================================================");
        System.out.println("            RIDEFARE: DYNAMIC SURGE PRICING ENGINE (PROJECT CP-044)             ");
        System.out.println("================================================================================");
        System.out.println("Usage:");
        System.out.println("  java -jar RideFare.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --gui, -g    Launch the graphical desktop interface (Java Swing)");
        System.out.println("  --cli, -c    Launch the interactive console menu (Terminal mode)");
        System.out.println("  --help, -h   Display this help message and exit");
        System.out.println();
        System.out.println("Default behavior: Launches GUI if supported, otherwise CLI.");
        System.out.println("================================================================================");
    }
}
