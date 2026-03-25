package com.aitest.analytics.ai;
import org.springframework.stereotype.Component;
@Component
public class FailureClassifier {

    public String classify(String errorMessage) {

        if (errorMessage == null || errorMessage.isEmpty()) {
            return "UNKNOWN";
        }

        String error = errorMessage.toLowerCase();

        if (error.contains("no such element") || error.contains("nosuchelement") || error.contains("element not found")) {
            return "LOCATOR_FAILURE";
        } else if (error.contains("timeout") || error.contains("timed out")) {
            return "SYNC_ISSUE";
        } else if (error.contains("stale element") || error.contains("staleelement")) {
            return "STALE_ELEMENT";
        } else if (error.contains("connection") || error.contains("refused") || error.contains("unreachable")) {
            return "NETWORK_ISSUE";
        } else if (error.contains("assert")) {
            return "ASSERTION_FAILURE";
        } else if (error.contains("invalid") || error.contains("unauthorized") || error.contains("login")) {
            return "AUTHENTICATION_FAILURE";
        } else if (error.contains("nullpointer") || error.contains("null pointer")) {
            return "NULL_POINTER";
        } else if (error.contains("webdriver") || error.contains("session")) {
            return "WEBDRIVER_FAILURE";
        }

        return "UNKNOWN";
    }
}