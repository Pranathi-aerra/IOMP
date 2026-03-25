package com.aitest.analytics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aitest.analytics.ai.RuleEngine;
import com.aitest.analytics.model.FailureLog;
import com.aitest.analytics.repository.FailureLogRepository;

import java.util.List;
import java.util.Map;

@Service
public class FailureAnalysisService {

    @Autowired
    private RuleEngine engine; // ✅ injected, NOT manually created

    @Autowired
    private FailureLogRepository failureLogRepository;

    public Map<String, String> analyzeFailure(String errorMessage) {

        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            return Map.of(
                "category", "UNKNOWN",
                "suggestion", "No error message provided."
            );
        }

        Map<String, String> result = engine.analyze(errorMessage);

        // ✅ Save to failure log
        try {
            FailureLog log = new FailureLog();
            log.setErrorMessage(errorMessage);
            log.setCategory(result.get("category"));
            log.setSuggestion(result.get("suggestion"));
            log.setStatus("FAIL");
            failureLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Failed to save failure log: " + e.getMessage());
        }

        return result;
    }

    public List<FailureLog> getAllFailureLogs() {
        return failureLogRepository.findAll();
    }

    public List<FailureLog> getLogsByCategory(String category) {
        return failureLogRepository.findByCategory(category);
    }
}