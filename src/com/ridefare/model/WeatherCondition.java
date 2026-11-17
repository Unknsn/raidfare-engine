package com.ridefare.model;

/**
 * ============================================================================
 * Domain Model: WeatherCondition.java
 * ============================================================================
 * 
 * ROLE AND PURPOSE:
 * Encapsulates meteorological atmospheric conditions and their quantitative
 * demand impact multipliers within the pricing engine.
 * 
 * METEOROLOGICAL IMPACT ON RIDE-HAILING DYNAMICS:
 * Inclement weather creates a dual economic shock:
 * 1. Demand Spikes: Pedestrians, cyclists, and transit users switch to ride-hailing
 *    to avoid rain, cold, and storms.
 * 2. Supply Drops: Slower vehicle speeds, reduced visibility, road waterlogging,
 *    and driver safety precautions decrease active driver availability.
 * 
 * MULTIPLIER SCHEDULE:
 * ----------------------------------------------------------------------------
 * Condition Enum | Display Title                  | Demand Impact Multiplier
 * ---------------+--------------------------------+-------------------------
 * CLEAR          | Clear / Normal                 | 1.00x (Neutral baseline)
 * RAIN           | Moderate Rain                  | 1.15x (+15% demand pressure)
 * HEAVY_RAIN     | Heavy Rain / Waterlogging      | 1.30x (+30% demand pressure)
 * STORM          | Severe Storm / Extreme Weather | 1.50x (+50% demand pressure)
 * ----------------------------------------------------------------------------
 * 
 * In Phase 2 of PricingEngine, this multiplier directly scales the raw demand
 * ratio to synthesize the overall Effective Demand Ratio (R_eff).
 * ============================================================================
 */
public enum WeatherCondition {

    /**
     * Dry, optimal driving conditions. Standard demand baseline.
     */
    CLEAR("Clear / Normal", 1.00),

    /**
     * Intermittent or moderate rainfall. Commuters shift off footpaths and bikes.
     */
    RAIN("Moderate Rain", 1.15),

    /**
     * Heavy precipitation and localized waterlogging. Traffic deceleration.
     */
    HEAVY_RAIN("Heavy Rain / Waterlogging", 1.30),

    /**
     * Severe convective storm, flash flooding, or dangerous squall.
     * High emergency transit demand coupled with severe driver scarcity.
     */
    STORM("Severe Storm / Extreme Weather", 1.50);

    /** User-friendly name displayed in UI comboboxes and console outputs */
    private final String displayName;

    /** Multiplicative factor applied to the raw demand-to-supply ratio */
    private final double impactMultiplier;

    /**
     * Enum constructor binding meteorological parameters.
     *
     * @param displayName      Descriptive string for UI presentation
     * @param impactMultiplier Numerical demand scalar (>= 1.00)
     */
    WeatherCondition(String displayName, double impactMultiplier) {
        this.displayName = displayName;
        this.impactMultiplier = impactMultiplier;
    }

    /**
     * Retrieves the formatted description of the weather condition.
     * @return Formatted title (e.g., "Heavy Rain / Waterlogging")
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Retrieves the quantitative impact multiplier for demand synthesis.
     * @return Demand scaling factor (1.00 to 1.50)
     */
    public double getImpactMultiplier() {
        return impactMultiplier;
    }

    /**
     * Formats enum instance for Swing components and terminal display.
     * @return Display name
     */
    @Override
    public String toString() {
        return displayName;
    }
}
