package com.aitest.analytics.ai;

import com.aitest.analytics.model.AIRule;
import com.aitest.analytics.repository.AIRuleRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class RuleEngine {

    @Autowired
    private AIRuleRepository ruleRepository;

    @Autowired
    private FailureClassifier failureClassifier;

    @Autowired
    private RootCauseAnalyzer rootCauseAnalyzer;

    @Value("${ollama.url:http://localhost:11434}")
    private String ollamaUrl;

    @Value("${ollama.model:llama3.2:latest}")
    private String ollamaModel;

    // ✅ PRIORITY 1 — Most specific (exact phrases, checked first)
    private static final Map<String, String[]> PRIORITY_1 = new LinkedHashMap<>() {{
        put("stale element reference",   new String[]{"STALE_ELEMENT",          "Element became stale. Re-locate element before interaction."});
        put("element click intercepted", new String[]{"LOCATOR_FAILURE",        "Another element is blocking the click. Scroll into view or use JS click."});
        put("no such element",           new String[]{"LOCATOR_FAILURE",        "Element not found. Check ID, XPath or CSS selector is correct."});
        put("unable to locate element",  new String[]{"LOCATOR_FAILURE",        "Element could not be located. Verify locator strategy and visibility."});
        put("element not interactable",  new String[]{"LOCATOR_FAILURE",        "Element exists but is not interactable. Wait for it to be enabled/visible."});
        put("invalid session id",        new String[]{"WEBDRIVER_FAILURE",      "WebDriver session is invalid. Restart the browser driver."});
        put("session not created",       new String[]{"WEBDRIVER_FAILURE",      "Could not create session. Check browser and driver version compatibility."});
        put("chrome not reachable",      new String[]{"WEBDRIVER_FAILURE",      "Chrome is not reachable. Restart ChromeDriver."});
        put("connection refused",        new String[]{"NETWORK_ISSUE",          "Connection refused. Check if the target server is running."});
        put("net::err_connection",       new String[]{"NETWORK_ISSUE",          "Network error. Check URL and internet connectivity."});
        put("assertionerror",            new String[]{"ASSERTION_FAILURE",      "Assertion error. Check expected values match actual output."});
        put("assertion failed",          new String[]{"ASSERTION_FAILURE",      "Assertion failed. Verify expected vs actual values in your test."});
        put("invalid credentials",       new String[]{"AUTHENTICATION_FAILURE", "Invalid credentials. Check username and password are correct."});
        put("unauthorized",              new String[]{"AUTHENTICATION_FAILURE", "Unauthorized. Verify login credentials and session tokens."});
        put("nullpointerexception",      new String[]{"NULL_POINTER",           "NullPointerException. Add null checks before accessing properties."});
        put("null pointer",              new String[]{"NULL_POINTER",           "Null pointer. Check object initialization before use."});
    }};

    // ✅ PRIORITY 2 — Medium specific (checked second)
    private static final Map<String, String[]> PRIORITY_2 = new LinkedHashMap<>() {{
        put("timeout",         new String[]{"SYNC_ISSUE",              "Timeout. Increase explicit wait time or add retry logic."});
        put("timed out",       new String[]{"SYNC_ISSUE",              "Operation timed out. Use WebDriverWait with ExpectedConditions."});
        put("waiting for",     new String[]{"SYNC_ISSUE",              "Wait condition failed. Increase timeout or check element condition."});
        put("stale",           new String[]{"STALE_ELEMENT",           "Stale element. Re-find element before each interaction."});
        put("connection",      new String[]{"NETWORK_ISSUE",           "Connection issue. Check server availability and network settings."});
        put("unreachable",     new String[]{"NETWORK_ISSUE",           "URL unreachable. Verify the target URL is accessible."});
        put("assert",          new String[]{"ASSERTION_FAILURE",       "Test assertion failed. Verify expected vs actual values."});
        put("login",           new String[]{"AUTHENTICATION_FAILURE",  "Login issue. Check credentials and authentication flow."});
        put("authentication",  new String[]{"AUTHENTICATION_FAILURE",  "Auth failed. Verify login logic and session handling."});
        put("webdriver",       new String[]{"WEBDRIVER_FAILURE",       "WebDriver error. Check browser driver version compatibility."});
        put("session",         new String[]{"WEBDRIVER_FAILURE",       "Session issue. Restart the WebDriver session."});
    }};

    // ✅ PRIORITY 3 — Broad (last resort before Ollama)
    private static final Map<String, String[]> PRIORITY_3 = new LinkedHashMap<>() {{
        put("element",   new String[]{"LOCATOR_FAILURE", "Element issue. Check locator strategy and page state."});
        put("null",      new String[]{"NULL_POINTER",    "Null reference. Add null checks in your test code."});
        put("invalid",   new String[]{"GENERAL_ERROR",   "Invalid operation. Check input values and application state."});
        put("failed",    new String[]{"GENERAL_ERROR",   "Operation failed. Check logs for more details."});
        put("exception", new String[]{"GENERAL_ERROR",   "Exception thrown. Review stack trace and fix root cause."});
        put("error",     new String[]{"GENERAL_ERROR",   "An error occurred. Check full stack trace for details."});
    }};

    public Map<String, String> analyze(String errorMessage) {

        Map<String, String> result = new HashMap<>();

        if (errorMessage == null || errorMessage.isEmpty()) {
            result.put("category", "GENERAL_ERROR");
            result.put("suggestion", "No error message provided.");
            return result;
        }

        String error = errorMessage.toLowerCase();

        // ── Step 1: Check MongoDB DB rules ──────────────────────────
        List<AIRule> rules = ruleRepository.findAll();
        for (AIRule rule : rules) {
            if (rule.getKeyword() != null && error.contains(rule.getKeyword().toLowerCase())) {
                System.out.println("✅ DB rule matched: " + rule.getKeyword());
                result.put("category", rule.getCategory());
                result.put("suggestion", rule.getSuggestion());
                return result;
            }
        }

        // ── Step 2: Priority 1 — Most specific ──────────────────────
        for (Map.Entry<String, String[]> entry : PRIORITY_1.entrySet()) {
            if (error.contains(entry.getKey())) {
                System.out.println("🔴 P1 matched: " + entry.getKey());
                result.put("category", entry.getValue()[0]);
                result.put("suggestion", entry.getValue()[1]);
                saveRule(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                return result;
            }
        }

        // ── Step 3: Priority 2 — Medium specific ────────────────────
        for (Map.Entry<String, String[]> entry : PRIORITY_2.entrySet()) {
            if (error.contains(entry.getKey())) {
                System.out.println("🟡 P2 matched: " + entry.getKey());
                result.put("category", entry.getValue()[0]);
                result.put("suggestion", entry.getValue()[1]);
                saveRule(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                return result;
            }
        }

        // ── Step 4: Priority 3 — Broad ──────────────────────────────
        for (Map.Entry<String, String[]> entry : PRIORITY_3.entrySet()) {
            if (error.contains(entry.getKey())) {
                System.out.println("🟠 P3 matched: " + entry.getKey());
                result.put("category", entry.getValue()[0]);
                result.put("suggestion", entry.getValue()[1]);
                saveRule(entry.getKey(), entry.getValue()[0], entry.getValue()[1]);
                return result;
            }
        }

        // ── Step 5: No match → Call Ollama AI ───────────────────────
        System.out.println("🤖 No priority match. Calling Ollama AI...");
        Map<String, String> aiResponse = callOllamaAPI(errorMessage);

        String category = aiResponse.get("category");
        String suggestion = aiResponse.get("suggestion");

        saveRule(extractKeyword(errorMessage), category, suggestion);

        result.put("category", category);
        result.put("suggestion", suggestion);
        return result;
    }

    // ✅ Save rule only if keyword doesn't already exist
    private void saveRule(String keyword, String category, String suggestion) {
        if (!ruleRepository.existsByKeyword(keyword)) {
            AIRule newRule = new AIRule(keyword, category, suggestion, "AI_GENERATED");
            ruleRepository.save(newRule);
            System.out.println("💾 New rule saved: " + keyword + " → " + category);
        } else {
            System.out.println("⚠️ Rule already exists: " + keyword + " — skipping");
        }
    }

    private Map<String, String> callOllamaAPI(String errorMessage) {

        try {
            System.out.println("🦙 Calling Ollama (" + ollamaModel + ")...");

            URL url = new URI(ollamaUrl + "/api/generate").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(60000);

            String cleanError = errorMessage
                .replace("\\", " ").replace("\"", " ")
                .replace("\n", " ").replace("\r", " ")
                .replace("\t", " ")
                .replaceAll("[\\p{Cntrl}]", " ")
                .replaceAll("\\s+", " ").trim();

            String prompt = "You are a Selenium test failure analyzer. "
                + "Analyze this error and respond with ONLY a JSON object, nothing else. "
                + "Format: {\"category\": \"CATEGORY\", \"suggestion\": \"detailed fix suggestion\"}. "
                + "Valid categories: LOCATOR_FAILURE, SYNC_ISSUE, STALE_ELEMENT, "
                + "NETWORK_ISSUE, ASSERTION_FAILURE, AUTHENTICATION_FAILURE, "
                + "NULL_POINTER, WEBDRIVER_FAILURE, GENERAL_ERROR. "
                + "Error: " + cleanError;

            String safePrompt = prompt.replace("\"", "'");

            String requestBody = "{"
                + "\"model\": \"" + ollamaModel + "\","
                + "\"prompt\": \"" + safePrompt + "\","
                + "\"stream\": false"
                + "}";

            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("📡 Ollama Response Code: " + responseCode);

            if (responseCode != 200) {
                System.out.println("⚠️ Ollama unavailable. Using local fallback...");
                return localFallback(errorMessage);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) responseBuilder.append(line);
            br.close();

            String response = responseBuilder.toString();
            System.out.println("📨 Ollama Raw Response: " + response);

            // ✅ Use Jackson to parse Ollama response
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            String innerJson = root.has("response") ? root.get("response").asText() : "";
            System.out.println("📝 Inner JSON: " + innerJson);

            innerJson = innerJson.replace("'", "\"");
            JsonNode inner = mapper.readTree(innerJson);
            String category = inner.has("category") ? inner.get("category").asText() : "";
            String suggestion = inner.has("suggestion") ? inner.get("suggestion").asText() : "";

            if (category.isEmpty() || "UNKNOWN".equals(category)) {
                System.out.println("⚠️ Could not parse Ollama response. Using local fallback...");
                return localFallback(errorMessage);
            }

            System.out.println("🏷️  Category: " + category);
            System.out.println("💡 Suggestion: " + suggestion);

            Map<String, String> result = new HashMap<>();
            result.put("category", category);
            result.put("suggestion", suggestion);
            return result;

        } catch (Exception e) {
            System.err.println("❌ Ollama call failed: " + e.getMessage());
            return localFallback(errorMessage);
        }
    }

    // ✅ Delegates to FailureClassifier + RootCauseAnalyzer
    private Map<String, String> localFallback(String errorMessage) {
        String category = failureClassifier.classify(errorMessage);
        String suggestion = rootCauseAnalyzer.suggest(category);

        Map<String, String> result = new HashMap<>();
        result.put("category", category);
        result.put("suggestion", suggestion);

        System.out.println("🔄 Fallback used → Category: " + category);
        return result;
    }

    private String extractKeyword(String errorMessage) {
        if (errorMessage == null) return "unknown";

        String error = errorMessage.toLowerCase();
        if (error.contains("no such element"))    return "no such element";
        if (error.contains("element located"))    return "element located";
        if (error.contains("timeout"))            return "timeout";
        if (error.contains("stale element"))      return "stale element";
        if (error.contains("connection refused")) return "connection refused";
        if (error.contains("invalid session"))    return "invalid session";
        if (error.contains("unauthorized"))       return "unauthorized";
        if (error.contains("null"))               return "null pointer";

        String[] stopWords = {"milliseconds", "seconds", "element", "waiting",
                              "tried", "interval", "with", "for", "the", "by",
                              "and", "expected", "condition", "failed", "located"};
        String[] words = errorMessage.split("\\s+");
        String keyword = "general_error";
        for (String word : words) {
            String cleaned = word.replaceAll("[^a-zA-Z]", "").toLowerCase();
            boolean isStop = false;
            for (String stop : stopWords) {
                if (cleaned.equals(stop)) { isStop = true; break; }
            }
            if (!isStop && cleaned.length() > 5) { keyword = cleaned; break; }
        }
        return keyword;
    }
}