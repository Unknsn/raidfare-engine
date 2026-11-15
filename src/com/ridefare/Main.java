package com.ridefare;

import com.ridefare.ui.CommandLineUI;
import com.ridefare.ui.GraphicalUI;

import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/**
 * Main application entry point for the RideFare Dynamic Surge Pricing Engine.
 * Supports dual-mode execution: Graphical User Interface (Swing) and Command Line Interface (CLI).
 */
public class Main {

    public static void main(String[] args) {
        boolean forceCli = false;
        boolean forceGui = false;

        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                forceCli = true;
            } else if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                forceGui = true;
            } else if ("--help".equalsIgnoreCase(arg) || "-h".equalsIgnoreCase(arg)) {
                printUsage();
                return;
            }
        }

        if (forceCli) {
            launchCli();
        } else if (forceGui) {
            launchGui();
        } else {
            // Intelligent automatic mode selection:
            // If headless environment, fallback to CLI; otherwise launch modern GUI
            if (GraphicsEnvironment.isHeadless()) {
                System.out.println("[INFO] Headless console environment detected. Launching CLI mode...");
                launchCli();
            } else {
                launchGui();
            }
        }
    }

    private static void launchGui() {
        SwingUtilities.invokeLater(() -> {
            try {
                GraphicalUI gui = new GraphicalUI();
                gui.setVisible(true);
            } catch (Exception ex) {
                System.err.println("[WARN] Failed to initialize GUI: " + ex.getMessage());
                System.out.println("[INFO] Falling back to Command Line Interface...");
                launchCli();
            }
        });
    }

    private static void launchCli() {
        CommandLineUI cli = new CommandLineUI();
        cli.start();
    }

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
