package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.utils.CVSS31Calculator;

import java.util.*;

@RestController
@RequestMapping("/api")
public class CVSSController {

    @PostMapping("/calculate")
    public Map<String, Double> calculateCVSS(@RequestBody Map<String, String> payload) {
        String vector = payload.get("vector");
        if (vector == null || vector.isBlank()) {
            throw new IllegalArgumentException("Vector string is required.");
        }
        double baseScore = CVSS31Calculator.calculateBaseScore(vector);
        double environmentalScore = CVSS31Calculator.calculateEnvironmentalScore(vector);
        return Map.of("baseScore", baseScore, "environmentalScore", environmentalScore);
    }

    @PostMapping("/upload")
    public List<Map<String, Object>> processFile(@RequestParam("file") MultipartFile file) throws Exception {
        List<Map<String, Object>> results = new ArrayList<>();
        String content = new String(file.getBytes());
        String[] vectors = content.split("\n");
        for (String vector : vectors) {
            try {
                double baseScore = CVSS31Calculator.calculateBaseScore(vector.trim());
                double environmentalScore = CVSS31Calculator.calculateEnvironmentalScore(vector.trim());
                results.add(Map.of("vector", vector, "baseScore", baseScore, "environmentalScore", environmentalScore));
            } catch (Exception e) {
                results.add(Map.of("vector", vector, "error", e.getMessage()));
            }
        }
        return results;
    }
}
