package com.aitest.analytics.repository;

import com.aitest.analytics.model.TestResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TestResultRepository extends MongoRepository<TestResult, String> {

    // All results newest first
    List<TestResult> findAllByOrderByCreatedAtDesc();

    // By suite URL — used by /api/test/suite?url=...
    // FIX: must match the normalized lowercase testSuite stored by TestExecutionService
    List<TestResult> findByTestSuiteOrderByCreatedAtDesc(String testSuite);

    List<TestResult> findByTestSuiteAndUserEmailOrderByCreatedAtDesc(String testSuite, String userEmail);
    List<TestResult> findByUserEmailOrderByCreatedAtDesc(String userEmail);

    // Filters
    List<TestResult> findByStatus(String status);
    List<TestResult> findByTestName(String testName);
    List<TestResult> findByCategory(String category);
    List<TestResult> findBySeverity(String severity);

    // Counts for stats
    long countByStatus(String status);
}