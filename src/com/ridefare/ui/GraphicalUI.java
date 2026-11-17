package com.ridefare.ui;

import com.ridefare.engine.FareValidator;
import com.ridefare.engine.PricingEngine;
import com.ridefare.model.FareBreakdown;
import com.ridefare.model.MarketConditions;
import com.ridefare.model.RideRequest;
import com.ridefare.model.VehicleType;
import com.ridefare.model.WeatherCondition;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * ============================================================================
 * Presentation Layer: GraphicalUI.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Provides a clean, modern, and practical Java Swing desktop interface for
 * the RideFare Dynamic Surge Pricing Engine.
 * 
 * DESIGN PRINCIPLES:
 * 1. Minimal & Functional Layout: Clean two-column design separating input controls
 *    (left pane) from real-time calculations and diagnostic outputs (right pane).
 * 2. Neutral Palette: Professional slate, deep teal, and neutral gray tones
 *    avoiding artificial or distracting decorative elements.
 * 3. Responsive Component Placement: The Quick Benchmark Scenario selector is docked
 *    in an independent header container with full horizontal width, preventing
 *    any clipping or overlap by adjacent titled border groups.
 * 4. High-Contrast Typography: Action buttons feature explicit dark-contrast
 *    foreground colors for optimal legibility across native OS themes.
 * 5. Instant Visual Feedback: Surge multipliers and final fares are color-coded
 *    (green for normal, orange-red for active surge) to immediately communicate market status.
 * 6. Multi-Tier Comparative Card: Automatically displays side-by-side fares for
 *    Economy, Standard, and Premium tiers for every calculation.
 * ============================================================================
 */
public class GraphicalUI extends JFrame {

    /** Business logic pricing engine instance */
    private final PricingEngine engine;

    // ------------------------------------------------------------------------
    // USER INPUT COMPONENTS
    // ------------------------------------------------------------------------
    private JTextField txtPickup;
    private JTextField txtDestination;
    private JTextField txtDistance;
    private JTextField txtDuration;
    private JComboBox<VehicleType> cmbVehicle;

    private JTextField txtRequests;
    private JTextField txtDrivers;
    private JCheckBox chkPeakHour;
    private JComboBox<WeatherCondition> cmbWeather;
    private JComboBox<String> cmbPresets;

    // ------------------------------------------------------------------------
    // CALCULATION & DIAGNOSTIC OUTPUT COMPONENTS
    // ------------------------------------------------------------------------
    private JLabel lblBaseFare;
    private JLabel lblDistanceCharge;
    private JLabel lblTimeCharge;
    private JLabel lblNormalFare;
    private JLabel lblEffectiveRatio;
    private JLabel lblSurgeMultiplier;
    private JLabel lblSurgeDelta;
    private JLabel lblFinalFare;
    private JLabel lblDemandLevel;
    private JTextArea txtNotes;

    // Multi-tier vehicle comparison labels
    private JLabel lblEconomyCompare;
    private JLabel lblStandardCompare;
    private JLabel lblPremiumCompare;

    /**
     * Constructs the main application window and initializes all UI components.
     */
    public GraphicalUI() {
        super("RideFare - Dynamic Surge Pricing Engine (CP-044)");
        this.engine = new PricingEngine();

        // Apply native operating system look and feel
        initLookAndFeel();

        // Build UI component hierarchy
        initComponents();

        // Window properties and positioning
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(920, 720);
        setMinimumSize(new Dimension(860, 680));
        setLocationRelativeTo(null); // Center window on primary monitor
    }

    /**
     * Attempts to set the Java Swing Look and Feel to match the native operating system.
     */
    private void initLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fallback gracefully to default Metal/Ocean Swing Look & Feel
        }
    }

    /**
     * Assembles the root content pane and major layout panels.
     */
    private void initComponents() {
        JPanel contentPane = new JPanel(new BorderLayout(10, 10));
        contentPane.setBackground(new Color(245, 247, 250));
        contentPane.setBorder(new EmptyBorder(12, 16, 16, 16));
        setContentPane(contentPane);

        // 1. Top Header Banner
        JPanel headerPanel = createHeaderPanel();
        contentPane.add(headerPanel, BorderLayout.NORTH);

        // 2. Main Center Area (Split Left: Inputs, Right: Outputs)
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        centerPanel.setOpaque(false);

        JPanel leftPanel = createInputPanel();
        JPanel rightPanel = createOutputPanel();

        centerPanel.add(leftPanel);
        centerPanel.add(rightPanel);
        contentPane.add(centerPanel, BorderLayout.CENTER);

        // 3. Bottom Action Bar (Calculate & Reset buttons)
        JPanel bottomBar = createBottomBar();
        contentPane.add(bottomBar, BorderLayout.SOUTH);

        // Pre-fill initial form fields with Benchmark Scenario 1 (Low Demand)
        loadPreset(1);
    }

    /**
     * Creates the top branding header panel with title and project metadata.
     */
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 41, 59)); // Dark slate background
        header.setBorder(new EmptyBorder(12, 16, 12, 16));

        JLabel title = new JLabel("RideFare Dynamic Surge Pricing Engine");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Travel, Transport and Logistics (Project CP-044)");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(203, 213, 225));

        header.add(title, BorderLayout.NORTH);
        header.add(subtitle, BorderLayout.SOUTH);
        return header;
    }

    /**
     * Constructs the left-hand panel containing all input controls.
     * Includes the Quick Benchmark scenario dropdown, Ride parameters, and Market conditions.
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setOpaque(false);

        // Preset selector bar:
        // Positioned in an independent NORTH container with BorderLayout(4, 4)
        // so it has full horizontal width and is never overlapped by titled borders below it.
        JPanel presetPanel = new JPanel(new BorderLayout(4, 4));
        presetPanel.setOpaque(false);
        presetPanel.setBorder(new EmptyBorder(0, 2, 6, 2));

        JLabel lblPreset = new JLabel("Quick Benchmark Scenario:");
        lblPreset.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPreset.setForeground(new Color(51, 65, 85));

        cmbPresets = new JComboBox<>(new String[]{
                "Custom User Input",
                "1. Low Demand (Clear, 15 req / 30 drivers)",
                "2. Balanced Market (Clear, 25 req / 25 drivers)",
                "3. Peak Hour Rush (Clear, 60 req / 30 drivers, Peak)",
                "4. Heavy Rain Downpour (70 req / 35 drivers, Heavy Rain)",
                "5. Severe Storm Shortage (90 req / 20 drivers, Storm, Peak)",
                "6. Zero Drivers Supply Exhaustion (40 req / 0 drivers)"
        });
        cmbPresets.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        cmbPresets.addActionListener(e -> onPresetSelected());

        presetPanel.add(lblPreset, BorderLayout.NORTH);
        presetPanel.add(cmbPresets, BorderLayout.CENTER);
        panel.add(presetPanel, BorderLayout.NORTH);

        // Forms container holding the two input groups (Ride Info & Market Conditions)
        JPanel formsPanel = new JPanel();
        formsPanel.setLayout(new BoxLayout(formsPanel, BoxLayout.Y_AXIS));
        formsPanel.setOpaque(false);

        // Group 1: Ride Parameters
        JPanel rideGroup = new JPanel(new GridBagLayout());
        rideGroup.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "1. Ride Information",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(51, 65, 85)));
        rideGroup.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtPickup = new JTextField("Central Railway Station", 14);
        txtDestination = new JTextField("International Airport", 14);
        txtDistance = new JTextField("18.5", 8);
        txtDuration = new JTextField("32.0", 8);
        cmbVehicle = new JComboBox<>(VehicleType.values());
        cmbVehicle.setSelectedItem(VehicleType.STANDARD);

        addFormField(rideGroup, gbc, 0, "Pickup Location:", txtPickup);
        addFormField(rideGroup, gbc, 1, "Destination:", txtDestination);
        addFormField(rideGroup, gbc, 2, "Distance (km):", txtDistance);
        addFormField(rideGroup, gbc, 3, "Duration (mins):", txtDuration);
        addFormField(rideGroup, gbc, 4, "Vehicle Tier:", cmbVehicle);

        formsPanel.add(rideGroup);
        formsPanel.add(Box.createVerticalStrut(8));

        // Group 2: Market Conditions
        JPanel marketGroup = new JPanel(new GridBagLayout());
        marketGroup.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "2. Real-Time Market Conditions",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(51, 65, 85)));
        marketGroup.setOpaque(false);

        txtRequests = new JTextField("45", 8);
        txtDrivers = new JTextField("20", 8);
        chkPeakHour = new JCheckBox("Peak Hour Active (1.20x)");
        chkPeakHour.setSelected(true);
        chkPeakHour.setOpaque(false);
        cmbWeather = new JComboBox<>(WeatherCondition.values());
        cmbWeather.setSelectedItem(WeatherCondition.RAIN);

        addFormField(marketGroup, gbc, 0, "Ride Requests:", txtRequests);
        addFormField(marketGroup, gbc, 1, "Available Drivers:", txtDrivers);
        addFormField(marketGroup, gbc, 2, "Timing Status:", chkPeakHour);
        addFormField(marketGroup, gbc, 3, "Weather Condition:", cmbWeather);

        formsPanel.add(marketGroup);
        panel.add(formsPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Helper method to add labeled rows to GridBagLayout form containers.
     */
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.35;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.65;
        panel.add(field, gbc);
    }

    /**
     * Constructs the right-hand panel displaying fare breakdowns, diagnostics,
     * and the multi-tier vehicle comparison card.
     */
    private JPanel createOutputPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                "3. Fare Breakdown & Diagnostics",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12), new Color(51, 65, 85)));

        // Itemized financial grid
        JPanel breakdownGrid = new JPanel(new GridLayout(8, 2, 6, 4));
        breakdownGrid.setOpaque(false);
        breakdownGrid.setBorder(new EmptyBorder(6, 10, 6, 10));

        lblBaseFare = new JLabel("$ 0.00");
        lblDistanceCharge = new JLabel("$ 0.00");
        lblTimeCharge = new JLabel("$ 0.00");
        lblNormalFare = new JLabel("$ 0.00");
        lblEffectiveRatio = new JLabel("0.00");
        lblSurgeMultiplier = new JLabel("1.00x");
        lblSurgeDelta = new JLabel("$ 0.00");
        lblFinalFare = new JLabel("$ 0.00");

        Font valueFont = new Font("Segoe UI", Font.BOLD, 12);
        lblBaseFare.setFont(valueFont);
        lblDistanceCharge.setFont(valueFont);
        lblTimeCharge.setFont(valueFont);
        lblNormalFare.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEffectiveRatio.setFont(valueFont);
        lblSurgeMultiplier.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSurgeDelta.setFont(valueFont);

        // Prominent final fare styling (Deep teal green, 18pt bold)
        lblFinalFare.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblFinalFare.setForeground(new Color(15, 118, 110));

        addBreakdownRow(breakdownGrid, "Base Fare:", lblBaseFare);
        addBreakdownRow(breakdownGrid, "Distance Charge:", lblDistanceCharge);
        addBreakdownRow(breakdownGrid, "Time Charge:", lblTimeCharge);
        addBreakdownRow(breakdownGrid, "Normal Standard Fare:", lblNormalFare);
        addBreakdownRow(breakdownGrid, "Effective Demand Ratio:", lblEffectiveRatio);
        addBreakdownRow(breakdownGrid, "Surge Multiplier:", lblSurgeMultiplier);
        addBreakdownRow(breakdownGrid, "Surge Addition (Delta):", lblSurgeDelta);
        addBreakdownRow(breakdownGrid, "TOTAL FINAL FARE:", lblFinalFare);

        panel.add(breakdownGrid, BorderLayout.NORTH);

        // Center section: Status level & transparent diagnostic notes
        JPanel statusPanel = new JPanel(new BorderLayout(4, 4));
        statusPanel.setOpaque(false);
        statusPanel.setBorder(new EmptyBorder(4, 10, 4, 10));

        lblDemandLevel = new JLabel("Status: Ready to compute");
        lblDemandLevel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDemandLevel.setForeground(new Color(30, 41, 59));

        txtNotes = new JTextArea(3, 20);
        txtNotes.setEditable(false);
        txtNotes.setLineWrap(true);
        txtNotes.setWrapStyleWord(true);
        txtNotes.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        txtNotes.setBackground(new Color(241, 245, 249));
        txtNotes.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        statusPanel.add(lblDemandLevel, BorderLayout.NORTH);
        statusPanel.add(new JScrollPane(txtNotes), BorderLayout.CENTER);

        // Bottom section: Vehicle Tier Comparison Card
        JPanel compareCard = new JPanel(new GridLayout(3, 1, 2, 2));
        compareCard.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                "Vehicle Tier Comparison for This Ride",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 11), new Color(71, 85, 105)));
        compareCard.setOpaque(false);

        lblEconomyCompare = new JLabel("Economy: -");
        lblStandardCompare = new JLabel("Standard: -");
        lblPremiumCompare = new JLabel("Premium: -");

        Font cmpFont = new Font("Segoe UI", Font.PLAIN, 11);
        lblEconomyCompare.setFont(cmpFont);
        lblStandardCompare.setFont(cmpFont);
        lblPremiumCompare.setFont(cmpFont);

        compareCard.add(lblEconomyCompare);
        compareCard.add(lblStandardCompare);
        compareCard.add(lblPremiumCompare);

        statusPanel.add(compareCard, BorderLayout.SOUTH);
        panel.add(statusPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Helper method to add row pairs to the breakdown grid.
     */
    private void addBreakdownRow(JPanel grid, String label, JLabel valueLabel) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        grid.add(l);
        grid.add(valueLabel);
    }

    /**
     * Constructs the bottom action button toolbar.
     */
    private JPanel createBottomBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bar.setOpaque(false);

        // High-contrast button text: dark slate (new Color(15, 23, 42)) ensures
        // crystal-clear legibility on standard Windows gray button surfaces.
        JButton btnCalculate = new JButton("Calculate Dynamic Fare");
        btnCalculate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCalculate.setForeground(new Color(15, 23, 42));
        btnCalculate.setFocusPainted(false);
        btnCalculate.addActionListener(this::onCalculateClicked);

        JButton btnReset = new JButton("Reset Defaults");
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnReset.setForeground(new Color(51, 65, 85));
        btnReset.addActionListener(e -> loadPreset(1));

        bar.add(btnReset);
        bar.add(btnCalculate);
        return bar;
    }

    /**
     * Action event handler triggered when "Calculate Dynamic Fare" is clicked.
     * Collects inputs, invokes engine, and updates GUI displays.
     */
    private void onCalculateClicked(ActionEvent e) {
        try {
            // Read and sanitize input fields
            String pickup = txtPickup.getText().trim();
            String destination = txtDestination.getText().trim();
            double distance = Double.parseDouble(txtDistance.getText().trim());
            double duration = Double.parseDouble(txtDuration.getText().trim());
            VehicleType vehicle = (VehicleType) cmbVehicle.getSelectedItem();

            int requests = Integer.parseInt(txtRequests.getText().trim());
            int drivers = Integer.parseInt(txtDrivers.getText().trim());
            boolean peak = chkPeakHour.isSelected();
            WeatherCondition weather = (WeatherCondition) cmbWeather.getSelectedItem();

            // Construct immutable domain models
            RideRequest request = new RideRequest(pickup, destination, distance, duration, vehicle);
            MarketConditions conditions = new MarketConditions(requests, drivers, peak, weather);

            // Execute engine calculations
            FareBreakdown breakdown = engine.calculateFare(request, conditions);

            // Update user interface displays
            updateOutputDisplay(breakdown);
            updateComparisonDisplay(request, conditions);

        } catch (NumberFormatException nfe) {
            // Handle non-numeric text in numeric input fields
            JOptionPane.showMessageDialog(this,
                    "Please ensure numeric inputs (Distance, Duration, Requests, Drivers) contain valid numbers.",
                    "Input Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException iae) {
            // Handle defensive domain validation rule violations
            JOptionPane.showMessageDialog(this,
                    iae.getMessage(),
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            // Handle unexpected runtime exceptions
            JOptionPane.showMessageDialog(this,
                    "An unexpected error occurred: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Updates all labels in the Fare Breakdown and Status panels with calculated values.
     */
    private void updateOutputDisplay(FareBreakdown b) {
        lblBaseFare.setText(String.format("$ %.2f", b.getBaseFare()));
        lblDistanceCharge.setText(String.format("$ %.2f (%.1f km)", b.getDistanceCharge(), b.getRequest().getDistanceKm()));
        lblTimeCharge.setText(String.format("$ %.2f (%.1f min)", b.getTimeCharge(), b.getRequest().getDurationMinutes()));
        lblNormalFare.setText(String.format("$ %.2f", b.getNormalFare()));

        // Display effective ratio or infinite flag
        if (Double.isInfinite(b.getEffectiveDemandRatio())) {
            lblEffectiveRatio.setText("INFINITE (No Supply)");
        } else {
            lblEffectiveRatio.setText(String.format("%.2f", b.getEffectiveDemandRatio()));
        }

        // Color-code surge multiplier: orange-red for active surge, green for normal
        lblSurgeMultiplier.setText(String.format("%.2fx", b.getSurgeMultiplier()));
        if (b.getSurgeMultiplier() > 1.0) {
            lblSurgeMultiplier.setForeground(new Color(194, 65, 12)); // Red-orange
        } else {
            lblSurgeMultiplier.setForeground(new Color(22, 101, 52)); // Green
        }

        lblSurgeDelta.setText(String.format("+$ %.2f", b.getSurgeDelta()));
        lblFinalFare.setText(String.format("$ %.2f", b.getFinalFare()));

        lblDemandLevel.setText("Market Status: " + b.getDemandLevel());
        txtNotes.setText(b.getNotes());
    }

    /**
     * Computes and displays side-by-side fares for Economy, Standard, and Premium tiers.
     */
    private void updateComparisonDisplay(RideRequest baseReq, MarketConditions cond) {
        // Economy Tier calculation
        RideRequest reqEco = new RideRequest(baseReq.getPickupLocation(), baseReq.getDestination(),
                baseReq.getDistanceKm(), baseReq.getDurationMinutes(), VehicleType.ECONOMY);
        FareBreakdown bEco = engine.calculateFare(reqEco, cond);
        lblEconomyCompare.setText(String.format("• Economy: Normal: $%.2f | Surged: $%.2f (Delta: +$%.2f)",
                bEco.getNormalFare(), bEco.getFinalFare(), bEco.getSurgeDelta()));

        // Standard Tier calculation
        RideRequest reqStd = new RideRequest(baseReq.getPickupLocation(), baseReq.getDestination(),
                baseReq.getDistanceKm(), baseReq.getDurationMinutes(), VehicleType.STANDARD);
        FareBreakdown bStd = engine.calculateFare(reqStd, cond);
        lblStandardCompare.setText(String.format("• Standard: Normal: $%.2f | Surged: $%.2f (Delta: +$%.2f)",
                bStd.getNormalFare(), bStd.getFinalFare(), bStd.getSurgeDelta()));

        // Premium Tier calculation
        RideRequest reqPrem = new RideRequest(baseReq.getPickupLocation(), baseReq.getDestination(),
                baseReq.getDistanceKm(), baseReq.getDurationMinutes(), VehicleType.PREMIUM);
        FareBreakdown bPrem = engine.calculateFare(reqPrem, cond);
        lblPremiumCompare.setText(String.format("• Premium: Normal: $%.2f | Surged: $%.2f (Delta: +$%.2f)",
                bPrem.getNormalFare(), bPrem.getFinalFare(), bPrem.getSurgeDelta()));
    }

    /**
     * Listener invoked when the user selects a preset scenario from the dropdown.
     */
    private void onPresetSelected() {
        int index = cmbPresets.getSelectedIndex();
        if (index > 0) {
            loadPreset(index);
        }
    }

    /**
     * Loads preconfigured benchmark values into the form fields and triggers calculation.
     *
     * @param index Preset selection index (1 through 6)
     */
    private void loadPreset(int index) {
        switch (index) {
            case 1: // Low Demand (Abundant Drivers)
                txtPickup.setText("Residential Suburb");
                txtDestination.setText("Shopping Mall");
                txtDistance.setText("8.0");
                txtDuration.setText("18.0");
                cmbVehicle.setSelectedItem(VehicleType.STANDARD);
                txtRequests.setText("15");
                txtDrivers.setText("30");
                chkPeakHour.setSelected(false);
                cmbWeather.setSelectedItem(WeatherCondition.CLEAR);
                break;
            case 2: // Balanced Market
                txtPickup.setText("City Library");
                txtDestination.setText("Business Center");
                txtDistance.setText("10.5");
                txtDuration.setText("22.0");
                cmbVehicle.setSelectedItem(VehicleType.STANDARD);
                txtRequests.setText("25");
                txtDrivers.setText("25");
                chkPeakHour.setSelected(false);
                cmbWeather.setSelectedItem(WeatherCondition.CLEAR);
                break;
            case 3: // Peak Hour Rush
                txtPickup.setText("Financial District");
                txtDestination.setText("Metro Station");
                txtDistance.setText("12.0");
                txtDuration.setText("30.0");
                cmbVehicle.setSelectedItem(VehicleType.STANDARD);
                txtRequests.setText("60");
                txtDrivers.setText("30");
                chkPeakHour.setSelected(true);
                cmbWeather.setSelectedItem(WeatherCondition.CLEAR);
                break;
            case 4: // Heavy Rain Downpour
                txtPickup.setText("Central Station");
                txtDestination.setText("Tech Park");
                txtDistance.setText("15.0");
                txtDuration.setText("35.0");
                cmbVehicle.setSelectedItem(VehicleType.STANDARD);
                txtRequests.setText("70");
                txtDrivers.setText("35");
                chkPeakHour.setSelected(false);
                cmbWeather.setSelectedItem(WeatherCondition.HEAVY_RAIN);
                break;
            case 5: // Severe Storm Shortage
                txtPickup.setText("Airport Terminal 1");
                txtDestination.setText("Convention Hall");
                txtDistance.setText("22.0");
                txtDuration.setText("45.0");
                cmbVehicle.setSelectedItem(VehicleType.PREMIUM);
                txtRequests.setText("90");
                txtDrivers.setText("20");
                chkPeakHour.setSelected(true);
                cmbWeather.setSelectedItem(WeatherCondition.STORM);
                break;
            case 6: // Zero Drivers Available (Supply Exhaustion)
                txtPickup.setText("Downtown Crossing");
                txtDestination.setText("North Suburbs");
                txtDistance.setText("14.0");
                txtDuration.setText("28.0");
                cmbVehicle.setSelectedItem(VehicleType.STANDARD);
                txtRequests.setText("40");
                txtDrivers.setText("0");
                chkPeakHour.setSelected(false);
                cmbWeather.setSelectedItem(WeatherCondition.CLEAR);
                break;
        }

        // Automatically execute calculation for the loaded preset
        onCalculateClicked(null);
    }
}
