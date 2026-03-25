package com.aitest.analytics.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aitest.analytics.service.FailureAnalysisService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    @Autowired
    private FailureAnalysisService service;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyze(
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            String error = request.get("errorMessage");

            if (error == null || error.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "errorMessage is required");
                return ResponseEntity.badRequest().body(response);
            }

            Map<String, String> result = service.analyzeFailure(error);

            response.put("success", true);
            response.put("errorMessage", error);
            response.put("category", result.get("category"));
            response.put("suggestion", result.get("suggestion"));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Analysis failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "FailureAnalysisService");
        return ResponseEntity.ok(response);
    }
}