package com.aitest.analytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "testExecutions")
public class TestResult {

    @Id
    private String id;

    @Indexed
    private String testName;

    @Indexed
    private String status;           // "PASS" | "FAIL" | "UNKNOWN"

    private String errorMessage;
    private String category;         // set by AI rule engine on FAIL
    private String suggestion;       // set by AI rule engine on FAIL
    private String screenshotPath;

    @Indexed
    private String testSuite;        // normalized lowercase URL — matches what SeleniumRunner sends

    @Indexed
    private String severity;         // "CRITICAL" | "WARNING" | "INFO"

    @CreatedDate
    private LocalDateTime createdAt;

    public TestResult() {
        this.createdAt = LocalDateTime.now();
    }

    // ─── Getters & Setters ────────────────────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public String getTestSuite() { return testSuite; }
    public void setTestSuite(String testSuite) { this.testSuite = testSuite; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "TestResult{" +
            "id='" + id + '\'' +
            ", testName='" + testName + '\'' +
            ", status='" + status + '\'' +
            ", severity='" + severity + '\'' +
            ", category='" + category + '\'' +
            ", testSuite='" + testSuite + '\'' +
            ", createdAt=" + createdAt +
            '}';
    }
}