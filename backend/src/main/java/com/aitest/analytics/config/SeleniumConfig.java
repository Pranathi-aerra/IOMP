package com.aitest.analytics.config;

import org.springframework.context.annotation.Configuration;

/**
 * SeleniumConfig — WebDriver is now managed directly by SeleniumRunner
 * (created once per test run, headless, and quit after all tests complete).
 * No Spring-managed WebDriver bean is needed.
 */
@Configuration
public class SeleniumConfig {
    // Intentionally empty — driver lifecycle is handled in SeleniumRunner.runAllTestsOnUrl()
}