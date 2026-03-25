package com.aitest.analytics.repository;

import com.aitest.analytics.model.FailureLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailureLogRepository extends MongoRepository<FailureLog, String> {

    List<FailureLog> findByTestName(String testName);
    List<FailureLog> findByStatus(String status);
    List<FailureLog> findByCategory(String category);
}