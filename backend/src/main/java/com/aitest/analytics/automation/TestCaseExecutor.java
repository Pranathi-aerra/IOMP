package com.aitest.analytics.automation;

import org.springframework.stereotype.Component;

@Component
public class TestCaseExecutor {

    public void runAllTests(String url) {
        System.out.println("🤖 Running tests → " + url);
        SeleniumRunner.runAllTestsOnUrl(url);
    }
}