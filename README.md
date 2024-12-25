# CVSS Calculator

This project provides a CVSS 3.1 calculator that allows users to enter a vector string or upload a file of vector strings to display CVSS 3.1 scores.

## Usage Instructions

1. **Enter Vector String:** Input a CVSS 3.1 vector string directly into the text box.
    
2. **Upload File:** Alternatively, you can upload a file containing CVSS 3.1 vector strings.
    

Once you've provided the vector string or uploaded the file, the calculator will compute and display the CVSS 3.1 scores.

## Calculation Model

CVSS3.1 calculator uses a **hybrid scoring model** that combines multiplicative and additive approaches based on specific weights and formula components.

### Key Aspects of the Calculation Logic

The calculation is a **weighted product model with non-linear adjustments** to capture the nuanced relationships between different vulnerability metrics. This approach ensures that critical vulnerabilities with widespread impacts receive higher scores, while less impactful ones are scored appropriately.

1. **Multiplicative Components:**
    
    - The **Impact Sub-Score (ISS)** and **Exploitability** are calculated by multiplying individual weights (e.g., Confidentiality, Integrity, Availability for ISS).
        
    - This ensures that all contributing factors have a proportional effect on the final score.
        
2. **Additive Components:**
    
    - The final **Base Score** or **Environmental Score** is computed by adding the Impact and Exploitability components.
        
    - This addition reflects the combined severity of system impact and exploitation ease.
        
3. **Non-Linear Adjustments:**
    
    - Special adjustments (like the exponential penalty for low Impact scores when Scope is changed) are applied to account for edge cases.
        
    - Example: −3.25×(ISS−0.02)^{15} penalizes low-impact cases to avoid inflating scores.
        
4. **Thresholding:**
    
    - Scores are capped at a maximum of 10.0.
        
    - This ensures consistent interpretation across vulnerability assessments.
        
5. **Scope Coefficient:**
    
    - When Scope changes, a multiplier (1.08) is applied to emphasize its broader impact.
        

### Base Score Calculation

- **Impact Sub-Score (ISS):**
    
    - Measures the impact on **Confidentiality (C)**, **Integrity (I)**, and **Availability (A)**.
        
    - Formula: `ISS = 1 − ((1 − C) × (1 − I) × (1 − A))`
        
    - High values for `C`, `I`, or `A` indicate significant impacts, increasing the score.
        
- **Impact:**
    
    - Adjusted based on **Scope (S)**:
        
        - **Unchanged (U):** `Impact = S × ISS`
            
        - **Changed (C):** `Impact = S × (ISS − 0.029) − 3.25 × (ISS − 0.02)^{15}`
            
    - Impact measures the potential system-level consequences.
        
- **Exploitability:**
    
    - Represents ease of exploitation, considering:
        
        - **Attack Vector (AV)**
            
        - **Attack Complexity (AC)**
            
        - **Privileges Required (PR)**
            
        - **User Interaction (UI)**
            
    - Formula: `Exploitability = 8.22 × AV × AC × PR × UI`
        
- **Base Score:**
    
    - Combines Impact and Exploitability:
        
        - **Unchanged Scope (U):** `BaseScore = min(Impact + Exploitability, 10)`
            
        - **Changed Scope (C):** `BaseScore = min(1.08 × (Impact + Exploitability), 10)`
            

### Environmental Score Calculation

The Environmental Score calculation is similar to the Base Score but includes **Modified Metrics**:

- **Modified Attack Vector (MAV)**
    
- **Modified Attack Complexity (MAC)**
    
- **Modified Privileges Required (MPR)**
    
- **Modified User Interaction (MUI)**
    
- **Modified Scope (MS)**
    
- **Modified Confidentiality (MC)**
    
- **Modified Integrity (MI)**
    
- **Modified Availability (MA)**
    

#### **Modified Impact Sub-Score (MISS):**

Adjusts the ISS using Modified CIA values and an upper limit of 0.915 to account for realistic maximum impacts.

- Formula: `MISS = 1 - ((1 - MC) × (1 - MI) × (1 - MA))`
    
- This ensures the score reflects the potential maximum impact realistically.
    
#### **Modified Impact:**

Uses the same Scope-based adjustment as the Base Score.

- For **Unchanged Scope (U):** `Modified Impact = MS × MISS`
    
- For **Changed Scope (C):** `Modified Impact = MS × (MISS - 0.029) - 3.25 × (MISS - 0.02)^{15}`
    

#### **Modified Exploitability:**

Similar to Exploitability but uses Modified metrics.

- Formula: `Modified Exploitability = 8.22 × MAV × MAC × MPR × MUI`
    

#### **Environmental Score:**

Combines Modified Impact and Modified Exploitability, adjusted for Modified Scope.

- For **Unchanged Scope (U):** `EnvironmentalScore = min(Modified Impact + Modified Exploitability, 10)`
    
- For **Changed Scope (C):** `EnvironmentalScore = min(1.08 × (Modified Impact + Modified Exploitability), 10)`
