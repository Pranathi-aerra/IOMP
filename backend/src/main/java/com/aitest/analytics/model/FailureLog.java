package com.aitest.analytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "failureLogs")
public class FailureLog {

    @Id
    private String id;

    private String testName;
    private String errorMessage;
    private String category;
    private String suggestion;
    private String status;
    private String screenshotPath;
    private LocalDateTime timestamp;

    public FailureLog() {
        this.timestamp = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    @Override
    public String toString() {
        return "FailureLog{id='" + id + "', testName='" + testName
            + "', status='" + status + "', category='" + category
            + "', timestamp=" + timestamp + '}';
    }
}