package com.aitest.analytics.repository;

import com.aitest.analytics.model.ScreenshotData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScreenshotRepository extends MongoRepository<ScreenshotData, String> {

    List<ScreenshotData> findByTestName(String testName);
    List<ScreenshotData> findByStatus(String status);
}