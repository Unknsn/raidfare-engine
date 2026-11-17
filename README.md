# RideFare: Dynamic Surge Pricing Engine for Ride-Hailing

**Project ID:** CP-044  
**Domain:** Travel, Transport and Logistics (Intelligent Systems)  
**Currency Standard:** Indian Rupees (INR / ₹)  
**Difficulty Level:** Intermediate  
**Language:** Java (JDK 17+) — Pure Standard Edition (Zero External Dependencies)  
**Deliverables:** Executable JAR (`RideFare.jar`), Clean Source Code, Test Suite, Technical Documentation, and Comprehensive Academic PDF Report (`docs/RideFare_Technical_Report.pdf`)

---

## 1. Project Overview

**RideFare** is an academic software development project that implements an intelligent, deterministic, and transparent **Dynamic Surge Pricing Engine** for ride-hailing applications, calibrated for Indian transport operating economics (INR / ₹). In modern transportation platforms, balancing passenger demand with driver supply is critical to maintaining operational liquidity, preventing driver shortages, and regulating localized congestion.

The system dynamically calculates ride fares based on:
1. **Base Ride Fare** (standard fixed vehicle access fee in ₹).
2. **Distance Travelled** (per-kilometer tariff in ₹/km).
3. **Estimated Journey Duration** (per-minute transit time charge in ₹/min).
4. **Current Demand** (active passenger booking requests in the dispatch sector).
5. **Available Drivers** (active, unassigned drivers patrolling the dispatch sector).
6. **Peak-Hour Status** (temporal demand multiplier for commuter rush hours).
7. **Weather Conditions** (inclement weather impact multiplier: clear, rain, heavy rain, storm).
8. **Vehicle Tiers** (Economy Mini, Standard Sedan, Premium SUV).

---

## 2. Mathematical Pricing Model

The engine operates on a deterministic, piecewise continuous mathematical pricing algorithm designed to balance economic incentives with strict regulatory protections.

### 2.1 Baseline Un-surged Fare Calculation
$$\text{DistanceCharge} = \text{Distance (km)} \times \text{RatePerKm}$$
$$\text{TimeCharge} = \text{Duration (min)} \times \text{RatePerMin}$$
$$\text{NormalFare} = \text{BaseFare} + \text{DistanceCharge} + \text{TimeCharge}$$

#### Standard Indian Tariff Rate Cards (INR / ₹):
| Vehicle Category | Base Fare (₹) | Per Km Rate (₹/km) | Per Minute Rate (₹/min) |
| :--- | :---: | :---: | :---: |
| **Economy (Mini)** | ₹ 50.00 | ₹ 12.00 | ₹ 1.50 |
| **Standard (Sedan)** | ₹ 60.00 | ₹ 15.00 | ₹ 2.00 |
| **Premium (SUV)** | ₹ 100.00 | ₹ 22.00 | ₹ 3.00 |

---

### 2.2 Effective Demand Ratio ($R_{\text{eff}}$)
$$\text{RawDemandRatio } (R) = \frac{\text{Ride Requests}}{\text{Available Drivers}}$$
$$R_{\text{eff}} = R \times F_{\text{peak}} \times F_{\text{weather}}$$

- **Peak Hour Factor ($F_{\text{peak}}$):**
  - Standard Hours: `1.00x`
  - Peak Commute Hours: `1.20x` (+20% demand weighting)
- **Weather Impact Factor ($F_{\text{weather}}$):**
  - Clear / Normal: `1.00x`
  - Moderate Rain: `1.15x`
  - Heavy Rain / Waterlogging: `1.30x`
  - Severe Storm / Extreme Weather: `1.50x`

---

### 2.3 Piecewise Surge Multiplier Function ($S$)

$$\begin{cases} 
1.00\times & \text{if } R_{\text{eff}} \le 1.00 \text{ (Normal Demand)} \\
1.00 + 0.30 \times (R_{\text{eff}} - 1.00) & \text{if } 1.00 < R_{\text{eff}} \le 1.50 \text{ (Mild Surge)} \\
1.15 + 0.50 \times (R_{\text{eff}} - 1.50) & \text{if } 1.50 < R_{\text{eff}} \le 2.50 \text{ (High Surge)} \\
1.65 + 0.65 \times (R_{\text{eff}} - 2.50) & \text{if } R_{\text{eff}} > 2.50 \text{ (Extreme Surge)} \\
3.50\times & \text{if Available Drivers } = 0 \text{ (Supply Exhaustion)}
\end{cases}$$

**Bounded Safeguards:**
$$S_{\text{final}} = \max(1.00, \min(3.50, \text{round}(S, 2)))$$
$$\text{Final Fare} = \text{round}(\text{NormalFare} \times S_{\text{final}}, 2)$$
$$\text{Surge Delta} = \text{Final Fare} - \text{NormalFare}$$

*Regulatory Guarantee:* Dynamic pricing is bounded by a hard statutory ceiling of **3.50x** to eliminate algorithmic price gouging during crises.

---

## 3. Project Directory Structure

```
D:\CDC\
├── bin\                                    // Compiled JVM bytecode classes
├── docs\
│   └── RideFare_Technical_Report.pdf      // Comprehensive academic technical report
├── src\
│   └── com\
│       └── ridefare\
│           ├── Main.java                   // Dual-mode entry point (GUI/CLI launcher)
│           ├── engine\
│           │   ├── FareValidator.java      // Rigorous input validation & boundary checks
│           │   └── PricingEngine.java      // Mathematical surge pricing calculations
│           ├── model\
│           │   ├── FareBreakdown.java      // Itemized financial and diagnostic result
│           │   ├── MarketConditions.java   // Requests, drivers, peak hour, weather
│           │   ├── RideRequest.java        // Origin, destination, distance, duration, tier
│           │   ├── VehicleType.java        // Vehicle tier tariffs enum (INR / ₹)
│           │   └── WeatherCondition.java   // Environmental weather impact enum
│           └── ui\
│               ├── CommandLineUI.java      // Interactive terminal console interface (₹)
│               └── GraphicalUI.java        // Clean Java Swing desktop interface (₹)
├── test\
│   └── com\
│       └── ridefare\
│           └── RideFareTest.java           // Automated unit and regression test suite
├── RideFare.jar                            // Standalone executable JAR package
└── README.md                               // Technical project guide
```

---

## 4. Key Architectural Features

1. **Dual-Mode User Interface:**
   - **Java Swing Desktop GUI:** Minimalist, clean theme, live interactive inputs, automatic side-by-side vehicle tier comparison in ₹, and quick benchmark preset scenarios.
   - **Interactive CLI:** Menu-driven console interface designed for terminal environments, automated scripts, and evaluator demonstrations.
2. **High-Precision Financial Arithmetic:**
   - Utilizes `java.math.BigDecimal` with `RoundingMode.HALF_UP` to prevent IEEE 754 floating-point inaccuracies.
3. **Rigorous Input Validation:**
   - Validates empty strings, identical origin/destination, negative distance, zero distance, out-of-bound journeys (>500 km, >24 hrs), negative driver/request counters, and zero-driver supply exhaustion.
4. **Deterministic Behavior:**
   - Mathematically monotonic and 100% reproducible: identical inputs yield bit-for-bit identical outputs across runs.

---

## 5. Compilation and Execution Guide

### Prerequisites
- Java Development Kit (JDK) 17 or higher (tested on Java SE 26).
- Standard terminal (PowerShell, Command Prompt, or Bash).

### 5.1 Compiling from Source
```powershell
# Compile all source files and test suite with UTF-8 encoding
javac -encoding UTF-8 -d bin src/com/ridefare/*.java src/com/ridefare/*/*.java test/com/ridefare/*.java
```

### 5.2 Building the Executable JAR
```powershell
jar cfe RideFare.jar com.ridefare.Main -C bin com
```

### 5.3 Running the Application

#### Launching the Graphical User Interface (Default):
```powershell
java -jar RideFare.jar --gui
# or simply:
java -jar RideFare.jar
```

#### Launching the Command Line Interface (CLI):
```powershell
java -jar RideFare.jar --cli
```

#### Viewing Help / Command-Line Arguments:
```powershell
java -jar RideFare.jar --help
```

---

## 6. Automated Testing and Verification

The system includes a zero-dependency automated verification suite covering:
- **Baseline Tariff Calculations:** Un-surged standard fares for all vehicle tiers in ₹.
- **Supply & Demand Dynamics:** Low demand, balanced markets, mild surge, high surge, and extreme surge.
- **Environmental Compounding:** Peak hour multiplier, rainfall impact, and severe storm tests.
- **Edge Cases & Boundaries:** Zero available drivers, zero ride requests, minimum (0.1 km) and maximum (500 km) distance thresholds, exact threshold transitions ($R=1.0, 1.5, 2.5$).
- **Input Validation Exception Handling:** Rejection of negative distances, blank locations, negative drivers, etc.
- **Determinism & Monotonicity:** 100-iteration repeatability test and monotonic surge progression.

### Executing the Test Suite
```powershell
java -cp "bin" com.ridefare.RideFareTest
```

### Actual Test Execution Output
```
================================================================================
              RIDEFARE AUTOMATED TEST SUITE EXECUTION (CP-044 - INR / ₹)        
================================================================================

--- Category 1: Baseline Fare Calculations (INR / ₹) ---
  [PASS] Standard Fare Un-surged                       | Expected=250.00, Actual=250.00 | Base ₹60.00 + Dist ₹150.00 + Time ₹40.00
  [PASS] Standard Multiplier 1.00x                     | Expected=1.00, Actual=1.00 | Normal demand ratio 0.50
  [PASS] Final Fare Equals Normal Fare                 | Expected=250.00, Actual=250.00 | No surge delta added
  [PASS] Economy Tier Tariff                           | Expected=200.00, Actual=200.00 | Economy 10km, 20min (₹200.00)
  [PASS] Premium Tier Tariff                           | Expected=380.00, Actual=380.00 | Premium 10km, 20min (₹380.00)

--- Category 2: Supply and Demand Dynamics ---
  [PASS] Low Demand Multiplier                         | Expected=1.00, Actual=1.00 | Ratio 0.25 <= 1.0
  [PASS] Low Demand Level Description                  | Normal Demand (No Surge)
  [PASS] Balanced Market Multiplier                    | Expected=1.00, Actual=1.00 | Ratio 1.00 <= 1.0
  [PASS] Mild Surge Multiplier                         | Expected=1.09, Actual=1.09 | Ratio 1.30 -> S=1.09
  [PASS] Mild Surge Label                              | Mild Demand Surge
  [PASS] High Surge Multiplier                         | Expected=1.40, Actual=1.40 | Ratio 2.00 -> S=1.40
  [PASS] High Surge Label                              | High Demand Surge
  [PASS] Extreme Surge Multiplier                      | Expected=1.98, Actual=1.98 | Ratio 3.00 -> S=1.98
  [PASS] Extreme Surge Label                           | Extreme Demand Surge
  [PASS] Maximum Cap Multiplier                        | Expected=3.50, Actual=3.50 | Strict cap at 3.50x
  [PASS] Maximum Cap Enforced Label                    | Maximum Surge Cap Enforced (3.50x)

--- Category 3: Environmental & Peak Compounding ---
  [PASS] Peak Hour Effective Ratio                     | Expected=1.20, Actual=1.20 | Raw 1.0 * Peak 1.20
  [PASS] Peak Hour Multiplier                          | Expected=1.06, Actual=1.06 | Effective Ratio 1.20 -> S=1.06
  [PASS] Heavy Rain Effective Ratio                    | Expected=1.30, Actual=1.30 | Raw 1.0 * Rain 1.30
  [PASS] Heavy Rain Multiplier                         | Expected=1.09, Actual=1.09 | Effective Ratio 1.30 -> S=1.09
  [PASS] Peak + Storm Effective Ratio                  | Expected=3.60, Actual=3.60 | 2.0 * 1.20 * 1.50 = 3.60
  [PASS] Peak + Storm Multiplier                       | Expected=2.37, Actual=2.37 | Effective Ratio 3.60 -> S=2.37

--- Category 4: Edge Cases & Boundary Conditions ---
  [PASS] Zero Drivers Infinite Ratio                   | Infinite ratio handled
  [PASS] Zero Drivers Multiplier Capped                | Expected=3.50, Actual=3.50 | Safety maximum cap applied
  [PASS] Zero Drivers Informative Diagnostic           | Zero drivers currently available in the dispatch zone. Maximum surge cap (3.50x) enforced to attract drivers.
  [PASS] Zero Requests Multiplier                      | Expected=1.00, Actual=1.00 | Ratio 0.0 -> Multiplier 1.00
  [PASS] Minimum Distance Allowed                      | Boundary 0.1km accepted
  [PASS] Maximum Distance Allowed                      | Boundary 500km accepted
  [PASS] Boundary Ratio 1.00                           | Expected=1.00, Actual=1.00 | S = 1.00
  [PASS] Boundary Ratio 1.50                           | Expected=1.15, Actual=1.15 | S = 1.15
  [PASS] Boundary Ratio 2.50                           | Expected=1.65, Actual=1.65 | S = 1.65

--- Category 5: Input Validation & Exception Handling ---
  [PASS] Reject Empty Pickup                           | Caught expected: Pickup location cannot be empty.
  [PASS] Reject Empty Destination                      | Caught expected: Destination cannot be empty.
  [PASS] Reject Identical Origin/Destination           | Caught expected: Pickup location and destination cannot be identical.
  [PASS] Reject Negative Distance                      | Caught expected: Distance must be at least 0.1 km (received: -5.00 km).
  [PASS] Reject Zero Distance                          | Caught expected: Distance must be at least 0.1 km (received: 0.00 km).
  [PASS] Reject Negative Duration                      | Caught expected: Estimated duration must be at least 1.0 minutes (received: -15.0 mins).
  [PASS] Reject Negative Requests                      | Caught expected: Ride requests count cannot be negative (received: -5).
  [PASS] Reject Negative Drivers                       | Caught expected: Available drivers count cannot be negative (received: -3).

--- Category 6: Determinism & Monotonicity ---
  [PASS] Calculation Determinism (100 runs)            | Exact identical output on repeated runs
  [PASS] Monotonicity Across Demand Growth             | Surge multiplier never decreases as demand grows

================================================================================
                             TEST EXECUTION SUMMARY                             
================================================================================
Total Test Cases Executed: 41
Passed:                    41 (100.0%)
Failed:                    0
Execution Time:            82 ms
================================================================================
[SUCCESS] ALL TEST CASES PASSED WITH 100% INTEGRITY.
```

---

## 7. Sample Interactive Ride Calculation (INR / ₹)

Below is an actual transaction executed through the RideFare pricing engine:

```
================================================================================
                   ITEMIZED FARE ESTIMATE RECEIPT (INR / ₹)                     
================================================================================
Pickup Location:        Airport Terminal 1
Destination:            Downtown Hotel
Distance & Duration:    18.50 km | 35.0 minutes
Vehicle Category:       Standard (Sedan)

----------------------------- MARKET DIAGNOSTICS -------------------------------
Active Requests:        50
Available Drivers:      25
Raw Demand-Supply Ratio:2.00
Peak Hour Adjustment:   Active (1.20x)
Weather Condition:      Moderate Rain (1.15x impact)
Effective Demand Ratio: 2.76
Market Surge Level:     Extreme Demand Surge

----------------------------- FINANCIAL BREAKDOWN ------------------------------
(+) Base Fare:                    ₹    60.00
(+) Distance Charge (18.50 km):   ₹   277.50
(+) Time Charge (35.0 mins):      ₹    70.00
--------------------------------------------------------------------------------
Standard Normal Fare:             ₹   407.50
Surge Pricing Multiplier:             1.82x
Dynamic Surge Additional Charge: +₹   334.15
================================================================================
TOTAL PAYABLE FARE:               ₹   741.65
================================================================================
System Notice: Substantial shortage of drivers relative to passenger requests.
================================================================================
```

---

## 8. Academic Evaluation Notes
This software engineering project adheres to:
- Clean code architecture without bloated frameworks.
- Single Responsibility Principle (SRP) across model, engine, and UI packages.
- Zero external libraries or runtime dependencies.
- Complete documentation and reproduction instructions.
- Full comprehensive academic technical report in `docs/RideFare_Technical_Report.pdf`.
