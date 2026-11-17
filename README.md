# RideFare: Dynamic Surge Pricing Engine

**Project ID:** CP-044  
**Domain:** Travel, Transport and Logistics (Intelligent Systems)  
**Currency:** Indian Rupees (INR / ₹)  
**Interface:** Command Line Interface (CLI)  
**Difficulty:** Beginner to Intermediate  
**Language:** Pure Java (JDK 17+) — Zero External Dependencies  
**Execution:** Runs directly from source with `java src/com/ridefare/Main.java` (No JAR or build tool required!)  

---

## 1. Project Overview

**RideFare** is a clean, simple, and practical Java project that demonstrates how dynamic surge pricing works in ride-hailing services (like Ola and Uber).

When passenger demand increases or available drivers decrease, the system dynamically calculates a fair surge multiplier to balance supply and demand while strictly capping fares at a statutory maximum ceiling (3.50x) to protect passengers.

### Key Factors Considered:
1. **Base Starting Fare:** Fixed flag-down fee in Indian Rupees (₹).
2. **Distance Travelled:** Kilometers travelled multiplied by per-km rate (₹/km).
3. **Journey Duration:** Minutes spent in the ride multiplied by per-minute rate (₹/min).
4. **Current Demand:** Number of active passenger booking requests in the zone.
5. **Available Drivers:** Number of available drivers ready to accept rides.
6. **Peak Rush Hour:** Rush hour demand weighting multiplier (1.20x).
7. **Weather Impact:** Clear (1.00x), Rain (1.15x), Heavy Rain (1.30x), Storm (1.50x).
8. **Vehicle Categories:** Economy (Mini), Standard (Sedan), Premium (SUV).

---

## 2. Realistic Indian Tariff Rates (INR / ₹)

| Vehicle Category | Base Starting Fee (₹) | Rate Per Km (₹/km) | Rate Per Minute (₹/min) | Example: 10 km / 20 min Trip |
| :--- | :---: | :---: | :---: | :---: |
| **Economy (Mini)** | ₹ 50.00 | ₹ 12.00 | ₹ 1.50 | ₹ 50 + ₹ 120 + ₹ 30 = **₹ 200.00** |
| **Standard (Sedan)** | ₹ 60.00 | ₹ 15.00 | ₹ 2.00 | ₹ 60 + ₹ 150 + ₹ 40 = **₹ 250.00** |
| **Premium (SUV)** | ₹ 100.00 | ₹ 22.00 | ₹ 3.00 | ₹ 100 + ₹ 220 + ₹ 60 = **₹ 380.00** |

---

## 3. How the Pricing Algorithm Works

### Step 1: Calculate Normal (Unsurged) Fare
$$\text{Distance Charge} = \text{Distance (km)} \times \text{Rate Per Km}$$
$$\text{Time Charge} = \text{Duration (mins)} \times \text{Rate Per Minute}$$
$$\text{Normal Fare} = \text{Base Fare} + \text{Distance Charge} + \text{Time Charge}$$

### Step 2: Calculate Effective Demand Ratio ($R_{\text{eff}}$)
$$\text{Raw Ratio} = \frac{\text{Ride Requests}}{\text{Available Drivers}}$$
$$R_{\text{eff}} = \text{Raw Ratio} \times \text{Peak Factor} \times \text{Weather Factor}$$

- **Peak Factor:** 1.20x during rush hours; 1.00x normally.
- **Weather Factor:** Clear (1.00x), Rain (1.15x), Heavy Rain (1.30x), Storm (1.50x).

### Step 3: Piecewise Continuous Surge Multiplier Curve
- **Normal Demand ($R_{\text{eff}} \le 1.0$):** Multiplier = **$1.00\times$** (Standard fare).
- **Mild Demand ($1.0 < R_{\text{eff}} \le 1.5$):** Multiplier = **$1.00 + 0.30 \times (R_{\text{eff}} - 1.0)$**.
- **High Demand ($1.5 < R_{\text{eff}} \le 2.5$):** Multiplier = **$1.15 + 0.50 \times (R_{\text{eff}} - 1.5)$**.
- **Extreme Demand ($R_{\text{eff}} > 2.5$):** Multiplier = **$1.65 + 0.65 \times (R_{\text{eff}} - 2.5)$**.
- **Zero Drivers Available:** Multiplier = **$3.50\times$** (Special emergency cap).

### Consumer Protection Ceiling (Maximum Cap):
To prevent price gouging, the surge multiplier is **strictly capped at a maximum of 3.50x**.

$$\text{Final Fare} = \text{round}(\text{Normal Fare} \times S, 2)$$
$$\text{Surge Delta} = \text{Final Fare} - \text{Normal Fare}$$

---

## 4. Project Directory Structure

```
D:\CDC\
├── docs\
│   └── RideFare_Technical_Report.pdf      // Comprehensive 17-page academic technical report
├── src\
│   └── com\
│       └── ridefare\
│           ├── Main.java                   // Application launcher
│           ├── RideFareTest.java           // 41-case automated test suite
│           ├── engine\
│           │   ├── FareValidator.java      // Input validation (prevents negative/invalid inputs)
│           │   └── PricingEngine.java      // Dynamic pricing calculation (BigDecimal math)
│           ├── model\
│           │   ├── FareBreakdown.java      // Itemized receipt record in ₹
│           │   ├── MarketConditions.java   // Requests, drivers, peak hour, weather snapshot
│           │   ├── RideRequest.java        // Origin, destination, distance, duration, car tier
│           │   ├── VehicleType.java        // Vehicle categories enum in ₹ (Mini, Sedan, SUV)
│           │   └── WeatherCondition.java   // Weather conditions enum (Clear to Storm)
│           └── ui\
│               └── CommandLineUI.java      // Interactive CLI menu & ASCII receipt printer
├── .gitignore                              // Git ignore rules (ignores .class, .jar, bin)
└── README.md                               // Documentation and execution guide
```

---

## 5. How to Run (Direct Execution — No Build Tools Needed!)

### Run the Interactive CLI Application:
From the project root directory, run:
```bash
java src/com/ridefare/Main.java
```
*(Or if your terminal is already inside `src/com/ridefare`, simply run: `java Main.java`)*

### Show Usage Help:
```bash
java src/com/ridefare/Main.java --help
```

### Run the Automated Test Suite (41 Tests):
```bash
java src/com/ridefare/Main.java --test
```
*(Alternatively, run directly: `java src/com/ridefare/RideFareTest.java`)*

---

## 6. Interactive CLI Menu Walkthrough

When you launch `Main.java`, you see a clean terminal menu:

```text
================================================================================
            RIDEFARE: DYNAMIC SURGE PRICING ENGINE (PROJECT CP-044)             
               Domain: Travel, Transport and Logistics (INR / ₹)                
================================================================================

----------------------------- MAIN NAVIGATION MENU -----------------------------
  [1] Calculate Dynamic Ride Fare (Interactive Input)
  [2] Compare Fares Across Vehicle Tiers (Economy vs Standard vs Premium)
  [3] Run Benchmark Simulation Scenarios (Low, Normal, Peak, Extreme, Zero-Driver)
  [4] View Pricing Algorithm Formulas & Vehicle Rate Cards
  [5] Exit Application
--------------------------------------------------------------------------------
Enter your choice (1-5): 
```

### Sample Itemized Receipt (Option 1):
```text
================================================================================
                   ITEMIZED FARE ESTIMATE RECEIPT (INR / ₹)                     
================================================================================
Pickup Location:        Majestic
Destination:            Whitefield
Distance & Duration:    22.00 km | 50.0 minutes
Vehicle Category:       Standard (Sedan)

----------------------------- MARKET DIAGNOSTICS -------------------------------
Active Requests:        35
Available Drivers:      15
Raw Demand-Supply Ratio:2.33
Peak Hour Adjustment:   Active (1.20x)
Weather Condition:      Clear / Normal (1.00x impact)
Effective Demand Ratio: 2.80
Market Surge Level:     Extreme Demand Surge

----------------------------- FINANCIAL BREAKDOWN ------------------------------
(+) Base Fare:                    ₹    60.00
(+) Distance Charge (22.00 km):    ₹   330.00
(+) Time Charge (50.0 mins):      ₹   100.00
--------------------------------------------------------------------------------
Standard Normal Fare:             ₹   490.00
Surge Pricing Multiplier:             1.85x
Dynamic Surge Additional Charge: +₹   416.50
================================================================================
TOTAL PAYABLE FARE:               ₹   906.50
================================================================================
System Notice: Substantial shortage of drivers relative to passenger requests.
================================================================================
```

---

## 7. Automated Test Suite (100% Passing)

The project includes an automated test suite verifying all 41 operational and edge-case scenarios:

```text
================================================================================
              RIDEFARE AUTOMATED TEST SUITE EXECUTION (CP-044 - INR / ₹)        
================================================================================
Total Test Cases Executed: 41
Passed:                    41 (100.0%)
Failed:                    0
Execution Time:            445 ms
================================================================================
[SUCCESS] ALL TEST CASES PASSED WITH 100% INTEGRITY.
```

---

## 8. Why This Implementation is Student-Friendly

1. **Zero External Dependencies:** Built entirely with standard Java SE. No Maven, Gradle, or external libraries required.
2. **No Build Artifacts:** Runs directly from source (`java src/com/ridefare/Main.java`) without needing compilation steps, JAR files, or leaving `.class` files on disk.
3. **Exact Financial Accuracy:** Uses `BigDecimal` with Half-Up rounding to avoid floating-point math errors.
4. **Defensive Input Handling:** Validates trip distances, durations, and counts gracefully without runtime crashes.
5. **Clean Code & Clear Comments:** Every method and calculation is simple, readable, and clearly documented.
