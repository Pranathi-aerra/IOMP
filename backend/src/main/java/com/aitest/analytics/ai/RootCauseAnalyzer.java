package com.aitest.analytics.ai;

import org.springframework.stereotype.Component;

@Component
public class RootCauseAnalyzer {

    public String suggest(String category) {

        if (category == null || category.isEmpty()) {
            return "No category provided. Check logs for more details.";
        }

        switch (category) {

            case "LOCATOR_FAILURE":
                return "Element not found. Check XPath/CSS selector, ensure element is visible, or update locator strategy.";

            case "SYNC_ISSUE":
                return "Timing issue detected. Increase wait time, use explicit waits, or add retry logic.";

            case "STALE_ELEMENT":
                return "Element became stale. Re-locate the element before interaction and avoid caching element references.";

            case "NETWORK_ISSUE":
                return "Network/connection error. Check server availability, API endpoints, and firewall/proxy settings.";

            case "ASSERTION_FAILURE":
                return "Assertion failed. Verify expected vs actual values, check test data, and confirm application state.";

            case "AUTHENTICATION_FAILURE":
                return "Auth error detected. Verify credentials, check token expiry, and review login/session logic.";

            case "NULL_POINTER":
                return "Null reference encountered. Check object initialization and add null checks before usage.";

            case "WEBDRIVER_FAILURE":
                return "WebDriver session issue. Restart driver, check browser/driver version compatibility.";

            default:
                return "Unknown failure. Check full stack trace and application logs for more details.";
        }
    }
}