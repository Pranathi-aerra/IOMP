package com.aitest.analytics.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.aitest.analytics.model.TestResult;
import com.aitest.analytics.repository.TestResultRepository;

@Service
public class TestExecutionService {

    @Autowired
    private TestResultRepository repository;

    @Autowired
    private FailureAnalysisService analysisService;

    @Autowired
    private MongoTemplate mongoTemplate;

    // ─── Save a single test result (called from TestController /run) ──────────
    public TestResult saveResult(TestResult result) {

        result.setCreatedAt(LocalDateTime.now());

        // Sanitize testName
        if (result.getTestName() == null || result.getTestName().isEmpty()) {
            result.setTestName("Unknown Test");
        }

        // Sanitize errorMessage
        if (result.getErrorMessage() == null) {
            result.setErrorMessage("");
        }

        // Sanitize status
        if (result.getStatus() == null ||
            (!result.getStatus().equals("PASS") && !result.getStatus().equals("FAIL"))) {
            result.setStatus("UNKNOWN");
        }

        // Sanitize severity
        if (result.getSeverity() == null || result.getSeverity().isEmpty()) {
            result.setSeverity("INFO");
        }

        // Sanitize testSuite — always normalize to lowercase trimmed URL
        if (result.getTestSuite() != null) {
            result.setTestSuite(result.getTestSuite().trim().toLowerCase());
        }

        // Auto-analyze failures using the AI rule engine
        if ("FAIL".equals(result.getStatus()) &&
            result.getCategory() == null &&
            !result.getErrorMessage().isEmpty()) {
            try {
                Map<String, String> analysis = analysisService.analyzeFailure(result.getErrorMessage());
                result.setCategory(analysis.get("category"));
                result.setSuggestion(analysis.get("suggestion"));
            } catch (Exception e) {
                System.err.println("⚠️ Analysis failed: " + e.getMessage());
                result.setCategory("UNKNOWN");
                result.setSuggestion("Analysis unavailable. Check logs.");
            }
        }

        // Default category for PASS results
        if (result.getCategory() == null) {
            result.setCategory("N/A");
        }
        if (result.getSuggestion() == null) {
            result.setSuggestion("");
        }

        TestResult saved = repository.save(result);
        System.out.println("💾 Saved → id: " + saved.getId()
            + " | test: " + saved.getTestName()
            + " | status: " + saved.getStatus()
            + " | suite: " + saved.getTestSuite());
        return saved;
    }

    // ─── Fetch all results ordered by newest first ────────────────────────────
    public List<TestResult> getAllResults() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    // ─── Fetch results for a specific URL (testSuite) ────────────────────────
    // FIX: normalize the incoming url so it matches stored lowercase values
    public List<TestResult> getBySuite(String url) {
        if (url == null || url.trim().isEmpty()) return List.of();
        return repository.findByTestSuiteOrderByCreatedAtDesc(url.trim().toLowerCase());
    }

    // ─── Fetch all distinct tested URLs for the history dropdown ─────────────
    public List<String> getDistinctSuites() {
        return mongoTemplate.findDistinct("testSuite", TestResult.class, String.class);
    }

    public List<TestResult> getByStatus(String status) {
        return repository.findByStatus(status);
    }

    public List<TestResult> getByTestName(String testName) {
        return repository.findByTestName(testName);
    }

    public List<TestResult> getByCategory(String category) {
        return repository.findByCategory(category);
    }

    public Map<String, Long> getStats() {
        long total   = repository.count();
        long passed  = repository.countByStatus("PASS");
        long failed  = repository.countByStatus("FAIL");
        return Map.of("total", total, "passed", passed, "failed", failed);
    }

    public void deleteResult(String id) {
        repository.deleteById(id);
    }
}