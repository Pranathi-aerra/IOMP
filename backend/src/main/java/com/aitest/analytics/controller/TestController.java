package com.aitest.analytics.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.aitest.analytics.automation.TestCaseExecutor;
import com.aitest.analytics.model.TestResult;
import com.aitest.analytics.service.TestExecutionService;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @Autowired
    private TestExecutionService service;

    @Autowired
    private TestCaseExecutor executor;

    private String now() {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
    }

    // 🚀 START TESTS
    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestBody Map<String, String> req) {

        System.out.println("🔥 CONTROLLER HIT");

        String url = req.get("url");
        String userEmail = req.get("userEmail"); // Optional, from frontend session

        if (url == null || url.isEmpty()) {
            return ResponseEntity.badRequest().body("URL required");
        }

        String normalized = url.trim().toLowerCase();

        System.out.println(now() + " | 🚀 EXECUTE → " + normalized + " by " + userEmail);

        new Thread(() -> executor.runAllTests(normalized, userEmail)).start();

        return ResponseEntity.ok("Started");
    }

    // 🔥 RECEIVE RESULTS
    @PostMapping(value = "/run", consumes = "application/json")
    public ResponseEntity<?> save(@RequestBody Map<String, String> payload) {

        System.out.println(now() + " | 🔥 /run → " + payload);

        TestResult r = new TestResult();
        r.setTestName(payload.get("testName"));
        r.setStatus(payload.get("status"));
        r.setErrorMessage(payload.getOrDefault("errorMessage", ""));
        r.setScreenshotPath(payload.get("screenshotPath"));
        r.setSeverity(payload.getOrDefault("severity", "INFO"));
        r.setTestSuite(payload.get("testSuite"));
        r.setUserEmail(payload.get("userEmail")); // Map userEmail from runner

        service.saveResult(r);

        return ResponseEntity.ok("saved");
    }

    // 📊 APIs
    @GetMapping("/suites")
    public List<String> suites(@RequestParam(value = "userEmail", required = false) String userEmail) {
        System.out.println(now() + " | API /suites for " + userEmail);
        return service.getDistinctSuites(userEmail);
    }

    @GetMapping("/suite")
    public List<TestResult> suite(@RequestParam("name") String name, @RequestParam(value = "userEmail", required = false) String userEmail) {
        System.out.println(now() + " | API /suite → " + name + " for " + userEmail);
        return service.getBySuite(name, userEmail);
    }
}