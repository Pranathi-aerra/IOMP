package com.aitest.analytics.automation;

import org.springframework.stereotype.Component;

@Component
public class TestCaseExecutor {

    public void runAllTests(String url, String userEmail) {
        System.out.println("🤖 Running tests → " + url + " for user: " + userEmail);
        SeleniumRunner.runAllTestsOnUrl(url, userEmail);
    }
}