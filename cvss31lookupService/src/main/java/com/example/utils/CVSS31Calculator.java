package com.example.utils;

import java.util.HashMap;
import java.util.Map;

public class CVSS31Calculator {

    // Constants used in calculations
    private static final double EXPLOITABILITY_COEFFICIENT = 8.22;
    private static final double SCOPE_COEFFICIENT = 1.08;

    // Mapping for CVSS metrics to their respective weights
    private static final Map<String, Double> WEIGHTS_AV = Map.of(
            "N", 0.85, "A", 0.62, "L", 0.55, "P", 0.2
    );
    private static final Map<String, Double> WEIGHTS_AC = Map.of(
            "H", 0.44, "L", 0.77
    );
    private static final Map<String, Map<String, Double>> WEIGHTS_PR = Map.of(
            "U", Map.of("N", 0.85, "L", 0.62, "H", 0.27),
            "C", Map.of("N", 0.85, "L", 0.68, "H", 0.5)
    );
    private static final Map<String, Double> WEIGHTS_UI = Map.of(
            "N", 0.85, "R", 0.62
    );
    private static final Map<String, Double> WEIGHTS_S = Map.of(
            "U", 6.42, "C", 7.52
    );
    private static final Map<String, Double> WEIGHTS_CIA = Map.of(
            "N", 0.0, "L", 0.22, "H", 0.56
    );

    public static double calculateBaseScore(String vector) {
        // Parse the vector string to extract individual metric values
        ParsedResult parsedResult = parseVectorString(vector);
        if (!parsedResult.isValid) {
            System.err.println("Invalid vector string format.");
            return -1; // Indicate an error with a specific value (e.g., -1)
        }
        Map<String, String> metrics = parsedResult.metrics;

        // Retrieve metric values
        String av = metrics.get("AV");
        String ac = metrics.get("AC");
        String pr = metrics.get("PR");
        String ui = metrics.get("UI");
        String s = metrics.get("S");
        String c = metrics.get("C");
        String i = metrics.get("I");
        String a = metrics.get("A");

        // Calculate Impact Sub-Score (ISS)
        double iss = 1 - ((1 - WEIGHTS_CIA.get(c)) * (1 - WEIGHTS_CIA.get(i)) * (1 - WEIGHTS_CIA.get(a)));

        // Calculate Impact based on Scope (S)
        double impact = s.equals("U")
                ? WEIGHTS_S.get(s) * iss
                : WEIGHTS_S.get(s) * (iss - 0.029) - 3.25 * Math.pow(iss - 0.02, 15);

        // Calculate Exploitability
        double exploitability = EXPLOITABILITY_COEFFICIENT *
                WEIGHTS_AV.get(av) *
                WEIGHTS_AC.get(ac) *
                WEIGHTS_PR.get(s).get(pr) *
                WEIGHTS_UI.get(ui);

        // Calculate Base Score
        double baseScore;
        if (impact <= 0) {
            baseScore = 0;
        } else {
            baseScore = s.equals("U")
                    ? Math.min(impact + exploitability, 10)
                    : Math.min(SCOPE_COEFFICIENT * (impact + exploitability), 10);
        }

        // Return Base Score rounded to 1 decimal place
        return roundToDecimal(baseScore, 1);
    }

    public static double calculateEnvironmentalScore(String vector) {
        // Parse the vector string to extract individual metric values
        ParsedResult parsedResult = parseVectorString(vector);
        if (!parsedResult.isValid) {
            System.err.println("Invalid vector string format.");
            return -1; // Indicate an error with a specific value (e.g., -1)
        }
        Map<String, String> metrics = parsedResult.metrics;

        // Retrieve metric values with fallbacks
        String mav = metrics.getOrDefault("MAV", metrics.get("AV"));
        String mac = metrics.getOrDefault("MAC", metrics.get("AC"));
        String mpr = metrics.getOrDefault("MPR", metrics.get("PR"));
        String mui = metrics.getOrDefault("MUI", metrics.get("UI"));
        String ms = metrics.getOrDefault("MS", metrics.get("S"));
        String mc = metrics.getOrDefault("MC", metrics.get("C"));
        String mi = metrics.getOrDefault("MI", metrics.get("I"));
        String ma = metrics.getOrDefault("MA", metrics.get("A"));

        // Perform calculations (same logic as before)
        double miss = Math.min(1 - ((1 - WEIGHTS_CIA.get(mc)) * (1 - WEIGHTS_CIA.get(mi)) * (1 - WEIGHTS_CIA.get(ma))), 0.915);
        double modifiedImpact = ms.equals("U")
                ? WEIGHTS_S.get(ms) * miss
                : WEIGHTS_S.get(ms) * (miss - 0.029) - 3.25 * Math.pow(miss * 0.9731 - 0.02, 13);
        double modifiedExploitability = EXPLOITABILITY_COEFFICIENT *
                WEIGHTS_AV.get(mav) *
                WEIGHTS_AC.get(mac) *
                WEIGHTS_PR.get(ms).get(mpr) *
                WEIGHTS_UI.get(mui);

        double environmentalScore;
        if (modifiedImpact <= 0) {
            environmentalScore = 0;
        } else {
            environmentalScore = ms.equals("U")
                    ? Math.min(modifiedImpact + modifiedExploitability, 10)
                    : Math.min(SCOPE_COEFFICIENT * (modifiedImpact + modifiedExploitability), 10);
        }

        return roundToDecimal(environmentalScore, 1);
    }

    private static ParsedResult parseVectorString(String vector) {
        Map<String, String> metrics = new HashMap<>();
        String[] pairs = vector.split("/");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                metrics.put(keyValue[0], keyValue[1]);
            } else {
                return new ParsedResult(metrics, false); // Return invalid result if format is incorrect
            }
        }
        // Validate required metrics
        if (!metrics.containsKey("AV") || !metrics.containsKey("AC") ||
            !metrics.containsKey("PR") || !metrics.containsKey("UI") ||
            !metrics.containsKey("S") || !metrics.containsKey("C") ||
            !metrics.containsKey("I") || !metrics.containsKey("A")) {
            return new ParsedResult(metrics, false); // Return invalid result if required metrics are missing
        }
        return new ParsedResult(metrics, true); // Return valid result
    }

    private static class ParsedResult {
        Map<String, String> metrics;
        boolean isValid;

        ParsedResult(Map<String, String> metrics, boolean isValid) {
            this.metrics = metrics;
            this.isValid = isValid;
        }
    }

    private static double roundToDecimal(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.ceil(value * scale) / scale;
    }

    public static void main(String[] args) {
        // Test CVSS vector strings
        String vector1 = "AV:N/AC:L/PR:N/UI:N/S:U/C:H/I:H/A:H/MAV:N/MAC:L/MPR:N/MUI:N/MS:U/MC:H/MI:H/MA:H";
        String vector2 = "AV:A/AC:H/PR:L/UI:R/S:C/C:L/I:L/A:L/MAV:A/MAC:H/MPR:L/MUI:R/MS:C/MC:L/MI:L/MA:L";
        String invalidVector = "INVALID_VECTOR_STRING";

        System.out.println("Vector: " + vector1 + "\nBase Score: " + calculateBaseScore(vector1));
        System.out.println("Vector: " + vector1 + "\nEnvironmental Score: " + calculateEnvironmentalScore(vector1));

        System.out.println("Vector: " + vector2 + "\nBase Score: " + calculateBaseScore(vector2));
        System.out.println("Vector: " + vector2 + "\nEnvironmental Score: " + calculateEnvironmentalScore(vector2));

        System.out.println("Vector: " + invalidVector + "\nBase Score: " + calculateBaseScore(invalidVector));
        System.out.println("Vector: " + invalidVector + "\nEnvironmental Score: " + calculateEnvironmentalScore(invalidVector));
    }
}
