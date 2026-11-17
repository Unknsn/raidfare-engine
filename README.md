# RideFare: Dynamic Surge Pricing Engine (CLI Edition)

**Project ID:** CP-044  
**Domain:** Travel, Transport and Logistics (Intelligent Systems)  
**Currency Standard:** Indian Rupees (INR / ₹)  
**Interface Mode:** Pure Command Line Interface (CLI)  
**Difficulty Level:** Beginner to Intermediate  
**Language:** Java (JDK 17+) — 100% Pure Standard Edition (Zero External Dependencies)  

---

## 1. Project Overview

**RideFare** is a clean, easy-to-understand Java academic project that demonstrates how modern ride-hailing platforms (like Ola and Uber) calculate dynamic ride fares. When many passengers want rides at the same time but few drivers are on the road, fares adjust dynamically ("surge pricing") to balance supply and demand.

The project is built as a **pure terminal application (CLI)** designed to be simple, transparent, and easy for any student or evaluator to run, understand, and test.

### What the Engine Considers:
1. **Base Ride Fare:** A fixed starting charge when the ride begins (₹).
2. **Distance Travelled:** Kilometers traveled multiplied by the rate per km (₹/km).
3. **Journey Duration:** Minutes spent on the road multiplied by the rate per minute (₹/min).
4. **Current Demand:** Active passenger booking requests in the dispatch area.
5. **Available Drivers:** Active, free drivers ready to accept rides.
6. **Peak Rush Hour:** Morning and evening office rush hour multiplier ($1.20\times$).
7. **Weather Impact:** Rain, heavy rain, or storm multipliers ($1.00\times$ to $1.50\times$).
8. **Vehicle Categories:** Economy (Mini), Standard (Sedan), and Premium (SUV).

---

## 2. Realistic Indian Tariff Rate Cards (INR / ₹)

The baseline tariffs are calibrated to typical Indian city ride-hailing rates:

| Vehicle Category | Base Starting Fee (₹) | Rate Per Km (₹/km) | Rate Per Minute (₹/min) | Example: 10 km / 20 min Trip |
| :--- | :---: | :---: | :---: | :---: |
| **Economy (Mini)** | ₹ 50.00 | ₹ 12.00 | ₹ 1.50 | ₹ 50 + ₹ 120 + ₹ 30 = **₹ 200.00** |
| **Standard (Sedan)** | ₹ 60.00 | ₹ 15.00 | ₹ 2.00 | ₹ 60 + ₹ 150 + ₹ 40 = **₹ 250.00** |
| **Premium (SUV)** | ₹ 100.00 | ₹ 22.00 | ₹ 3.00 | ₹ 100 + ₹ 220 + ₹ 60 = **₹ 380.00** |

---

## 3. How the Pricing Algorithm Works (3 Simple Steps)

### Step 1: Calculate Standard (Unsurged) Normal Fare
$$\text{Distance Charge} = \text{Distance (km)} \times \text{Rate Per Km}$$
$$\text{Time Charge} = \text{Duration (mins)} \times \text{Rate Per Minute}$$
$$\text{Normal Fare} = \text{Base Fare} + \text{Distance Charge} + \text{Time Charge}$$

### Step 2: Measure Market Demand Pressure ($R_{\text{eff}}$)
$$\text{Raw Ratio} = \frac{\text{Ride Requests}}{\text{Available Drivers}}$$
$$R_{\text{eff}} = \text{Raw Ratio} \times \text{Peak Hour Factor} \times \text{Weather Factor}$$

- **Peak Hour Factor:** `1.20x` during rush hours; `1.00x` normally.
- **Weather Factor:** Clear (`1.00x`), Moderate Rain (`1.15x`), Heavy Rain (`1.30x`), Severe Storm (`1.50x`).

### Step 3: Determine Surge Multiplier ($S$)
- **Normal Market ($R_{\text{eff}} \le 1.0$):** Multiplier = **$1.00\times$** (No extra charge).
- **Mild Demand ($1.0 < R_{\text{eff}} \le 1.5$):** Multiplier = **$1.00 + 0.30 \times (R_{\text{eff}} - 1.0)$**.
- **High Demand ($1.5 < R_{\text{eff}} \le 2.5$):** Multiplier = **$1.15 + 0.50 \times (R_{\text{eff}} - 1.5)$**.
- **Extreme Demand ($R_{\text{eff}} > 2.5$):** Multiplier = **$1.65 + 0.65 \times (R_{\text{eff}} - 2.5)$**.
- **Zero Drivers Available:** Multiplier = **$3.50\times$** (Special emergency cap).

### Consumer Protection Ceiling (Maximum Cap):
To protect passengers from unfair price gouging, the surge multiplier is **strictly capped at a maximum of 3.50x**.

$$\text{Final Fare} = \text{round}(\text{Normal Fare} \times S, 2)$$
$$\text{Surge Delta (Extra Charge)} = \text{Final Fare} - \text{Normal Fare}$$

---

## 4. Project Directory Structure

The project has a clean, focused, and minimal structure with no clutter:

```
D:\CDC\
├── docs\
│   └── RideFare_Technical_Report.pdf      // Comprehensive academic technical report
├── src\
│   └── com\
│       └── ridefare\
│           ├── Main.java                   // Application launcher
│           ├── engine\
│           │   ├── FareValidator.java      // Input validation (prevents negative values)
│           │   └── PricingEngine.java      // Core pricing algorithm (pure BigDecimal math)
│           ├── model\
│           │   ├── FareBreakdown.java      // Itemized receipt object
│           │   ├── MarketConditions.java   // Requests, drivers, peak hour, weather
│           │   ├── RideRequest.java        // Origin, destination, distance, duration, car
│           │   ├── VehicleType.java        // Car categories enum in ₹ (Mini, Sedan, SUV)
│           │   └── WeatherCondition.java   // Weather conditions enum (Clear to Storm)
│           └── ui\
│               └── CommandLineUI.java      // Interactive terminal menu & receipt printer
├── test\
│   └── com\
│       └── ridefare\
│           └── RideFareTest.java           // 41-case automated test suite
├── RideFare.jar                            // Standalone executable JAR package
└── README.md                               // Documentation guide
```

---

## 5. How to Compile and Run

### Prerequisites
- Any standard computer with Java installed (JDK 17 or higher).
- Command Prompt, PowerShell, or Linux Terminal.

### Option A: Run the Pre-built JAR (Easiest)
```bash
java -jar RideFare.jar
```

### Option B: Compile from Source Code
```powershell
# 1. Compile all Java files into a temporary build folder
javac -encoding UTF-8 -d build src/com/ridefare/*.java src/com/ridefare/*/*.java test/com/ridefare/*.java

# 2. Run the interactive CLI application
java -cp build com.ridefare.Main

# 3. Run the automated test suite
java -cp build com.ridefare.RideFareTest
```

---

## 6. Interactive CLI Menu Walkthrough

When you run the application, you see a clean, numbered menu:

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

### Option 1: Interactive Fare Calculation
Prompts you for trip details (pickup, drop, km, minutes, car tier) and live conditions (requests, drivers, peak, weather), then prints an itemized receipt:

```text
================================================================================
                   ITEMIZED FARE ESTIMATE RECEIPT (INR / ₹)                     
================================================================================
Pickup Location:        Central Railway Station
Destination:            International Airport
Distance & Duration:    18.50 km | 32.0 minutes
Vehicle Category:       Standard (Sedan)

----------------------------- MARKET DIAGNOSTICS -------------------------------
Active Requests:        45
Available Drivers:      20
Raw Demand-Supply Ratio:2.25
Peak Hour Adjustment:   Active (1.20x)
Weather Condition:      Moderate Rain (1.15x impact)
Effective Demand Ratio: 3.11
Market Surge Level:     Extreme Demand Surge

----------------------------- FINANCIAL BREAKDOWN ------------------------------
(+) Base Fare:                    ₹    60.00
(+) Distance Charge (18.50 km):   ₹   277.50
(+) Time Charge (32.0 mins):      ₹    64.00
--------------------------------------------------------------------------------
Standard Normal Fare:             ₹   401.50
Surge Pricing Multiplier:             2.05x
Dynamic Surge Additional Charge: +₹   421.58
================================================================================
TOTAL PAYABLE FARE:               ₹   823.08
================================================================================
System Notice: Substantial shortage of drivers relative to passenger requests.
================================================================================
```

### Option 2: Side-by-Side Vehicle Comparison
Compares all three tiers under the exact same journey and market conditions:

```text
================================================================================
FARE COMPARISON: 'Indiranagar' to 'Electronic City' (18.00 km, 40.0 mins)
Market: Requests=30, Drivers=15, Peak=true, Weather=Clear / Normal
================================================================================
Vehicle Tier       | Normal Fare  | Surge    | Final Fare   | Difference  
--------------------------------------------------------------------------------
Economy (Mini)     | ₹     326.00 |  1.60x   | ₹     521.60 | +₹    195.60
Standard (Sedan)   | ₹     410.00 |  1.60x   | ₹     656.00 | +₹    246.00
Premium (SUV)      | ₹     616.00 |  1.60x   | ₹     985.60 | +₹    369.60
================================================================================
```

### Option 3: Benchmark Simulation Scenarios
Instantly runs 6 industry standard scenarios (Low demand, Balanced, Peak commute, Monsoon downpour, Severe storm, and Zero drivers).

---

## 7. Automated Test Suite (41 Tests)

To ensure the code is rock-solid and completely bug-free, an automated test suite is included in `test/com/ridefare/RideFareTest.java`. It runs without needing any extra libraries like JUnit.

### How to Run Tests:
```powershell
java -cp RideFare.jar com.ridefare.RideFareTest
```

### Test Results Summary:
```text
================================================================================
              RIDEFARE AUTOMATED TEST SUITE EXECUTION (CP-044 - INR / ₹)        
================================================================================
Total Test Cases Executed: 41
Passed:                    41 (100.0%)
Failed:                    0
Execution Time:            82 ms
================================================================================
[SUCCESS] ALL TEST CASES PASSED WITH 100% INTEGRITY.
```

---

## 8. Why This Code is Great for Beginners

1. **Easy to Read:** Clear variable and method names (`baseFare`, `distanceCharge`, `calculateFare`).
2. **Well Commented:** Every class, method, and math formula is explained in simple English.
3. **Pure Java SE:** No complicated third-party frameworks, annotations, or XML configs.
4. **Accurate Math:** Uses `BigDecimal` with Half-Up rounding to avoid binary floating-point math mistakes.
5. **Safe & Defensive:** Handles division-by-zero safely if available drivers is 0.
