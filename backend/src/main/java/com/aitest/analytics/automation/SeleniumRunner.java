package com.aitest.analytics.automation;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;

import com.fasterxml.jackson.databind.ObjectMapper;



import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class SeleniumRunner {
    private static String now() {
    return java.time.LocalDateTime.now()
        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd:HH:mm:ss"));
}

private static void log(String msg) {
    System.out.println(now() + " | " + msg);
}

    private static final String BACKEND_URL = "http://localhost:8081";

    public static void runAllTestsOnUrl(String targetUrl) {
        System.out.println("\n🚀 Running all tests on: " + targetUrl);

        runPageLoadTest(targetUrl);
        runTitleVerificationTest(targetUrl);
        runElementPresenceTest(targetUrl);
        runButtonClickTest(targetUrl);
        runFormSubmissionTest(targetUrl);
        runBrokenLinksTest(targetUrl);
        runScreenshotTest(targetUrl);
        runMetaTagsTest(targetUrl);
        runImageLoadingTest(targetUrl);
        runPageScrollTest(targetUrl);
        runConsoleErrorTest(targetUrl);
        runJavaScriptErrorTest(targetUrl);
        runCookieTest(targetUrl);
        run404ErrorPageTest(targetUrl);
        runAccessibilityTest(targetUrl);
        runPerformanceMetricsTest(targetUrl);
        runDuplicateContentTest(targetUrl);

        System.out.println("\n✅ All 17 tests completed for: " + targetUrl);
    }

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : "https://example.com";
        runAllTestsOnUrl(url);
    }

    // ─── TEST 1: Page Load — CRITICAL ────────────────────────────
    public static void runPageLoadTest(String targetUrl) {
        String testName = "Page Load Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            long startTime = System.currentTimeMillis();
            driver.get(targetUrl);
            long loadTime = System.currentTimeMillis() - startTime;
            if (loadTime > 5000) {
                status = "FAIL";
                errorMessage = "Page load too slow: " + loadTime + "ms (threshold: 5000ms)";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Page loaded in " + loadTime + "ms"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "CRITICAL", targetUrl);
    }

    // ─── TEST 2: Title Verification — CRITICAL ───────────────────
    public static void runTitleVerificationTest(String targetUrl) {
        String testName = "Title Verification Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            String title = driver.getTitle();
            if (title == null || title.isEmpty()) {
                status = "FAIL"; errorMessage = "Page title is empty or missing";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Title: " + title); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "CRITICAL", targetUrl);
    }

    // ─── TEST 3: Element Presence — CRITICAL ─────────────────────
    public static void runElementPresenceTest(String targetUrl) {
        String testName = "Element Presence Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            String[] tags = {"body", "header", "nav", "main", "footer"};
            int found = 0;
            for (String tag : tags) {
                try { driver.findElement(By.tagName(tag)); found++; } catch (Exception ignored) {}
            }
            if (found == 0) {
                status = "FAIL"; errorMessage = "No common elements found";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Found " + found + " common elements"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "CRITICAL", targetUrl);
    }

    // ─── TEST 4: Button Click — WARNING ──────────────────────────
    public static void runButtonClickTest(String targetUrl) {
        String testName = "Button Click Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            if (buttons.isEmpty()) buttons = driver.findElements(By.cssSelector("input[type='button'],input[type='submit']"));
            if (buttons.isEmpty()) {
                status = "FAIL"; errorMessage = "No clickable buttons found on page";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Found " + buttons.size() + " buttons"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "WARNING", targetUrl);
    }

    // ─── TEST 5: Form Submission — WARNING ───────────────────────
    public static void runFormSubmissionTest(String targetUrl) {
        String testName = "Form Submission Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            if (forms.isEmpty() && inputs.isEmpty()) {
                status = "FAIL"; errorMessage = "No forms or input fields found on page";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Found " + forms.size() + " forms, " + inputs.size() + " inputs"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "WARNING", targetUrl);
    }

    // ─── TEST 6: Broken Links — CRITICAL ─────────────────────────
    public static void runBrokenLinksTest(String targetUrl) {
        String testName = "Broken Links Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            List<WebElement> links = driver.findElements(By.tagName("a"));
            int brokenCount = 0, checkedCount = 0;
            for (WebElement link : links) {
                if (checkedCount >= 10) break;
                String href = link.getAttribute("href");
                if (href == null || href.startsWith("#") || href.startsWith("javascript")) continue;
                try {
                    URL url = new URL(href);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("HEAD"); conn.setConnectTimeout(3000); conn.setReadTimeout(3000);
                    if (conn.getResponseCode() >= 400) brokenCount++;
                    checkedCount++;
                } catch (Exception ignored) {}
            }
            if (brokenCount > 0) {
                status = "FAIL"; errorMessage = "Found " + brokenCount + " broken links out of " + checkedCount + " checked";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ No broken links in " + checkedCount + " checked"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "CRITICAL", targetUrl);
    }

    // ─── TEST 7: Screenshot — INFO ───────────────────────────────
    public static void runScreenshotTest(String targetUrl) {
        String testName = "Screenshot Capture Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            Thread.sleep(2000);
            screenshotPath = takeScreenshot(driver, testName);
            if (screenshotPath.isEmpty()) { status = "FAIL"; errorMessage = "Failed to capture screenshot"; }
            else { System.out.println("✅ Screenshot: " + screenshotPath); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "INFO", targetUrl);
    }

    // ─── TEST 8: Meta Tags — INFO ─────────────────────────────────
    public static void runMetaTagsTest(String targetUrl) {
        String testName = "Meta Tags Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            List<WebElement> metaTags = driver.findElements(By.tagName("meta"));
            boolean hasDescription = false, hasViewport = false, hasCharset = false;
            for (WebElement meta : metaTags) {
                String name = meta.getAttribute("name");
                String charset = meta.getAttribute("charset");
                String content = meta.getAttribute("content");
                if ("description".equalsIgnoreCase(name) && content != null && !content.isEmpty()) hasDescription = true;
                if ("viewport".equalsIgnoreCase(name)) hasViewport = true;
                if (charset != null && !charset.isEmpty()) hasCharset = true;
            }
            StringBuilder missing = new StringBuilder();
            if (!hasDescription) missing.append("meta description, ");
            if (!hasViewport) missing.append("viewport meta, ");
            if (!hasCharset) missing.append("charset meta, ");
            if (missing.length() > 0) {
                status = "FAIL"; errorMessage = "Missing: " + missing.toString().replaceAll(", $", "");
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ All meta tags present"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "INFO", targetUrl);
    }

    // ─── TEST 9: Image Loading — WARNING ──────────────────────────
    public static void runImageLoadingTest(String targetUrl) {
        String testName = "Image Loading Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            List<WebElement> images = driver.findElements(By.tagName("img"));
            int brokenImages = 0, missingAlt = 0;
            for (WebElement img : images) {
                Boolean loaded = (Boolean)((JavascriptExecutor) driver)
                    .executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0", img);
                if (!loaded) brokenImages++;
                String alt = img.getAttribute("alt");
                if (alt == null || alt.isEmpty()) missingAlt++;
            }
            if (brokenImages > 0) {
                status = "FAIL";
                errorMessage = brokenImages + " broken images out of " + images.size()
                    + " total. (" + missingAlt + " missing alt text — warning only)";
                screenshotPath = takeScreenshot(driver, testName);
            } else if (missingAlt > 0) {
                status = "PASS";
                errorMessage = missingAlt + " images missing alt text (accessibility warning)";
            } else { System.out.println("✅ All " + images.size() + " images loaded correctly"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "WARNING", targetUrl);
    }

    // ─── TEST 10: Page Scroll — INFO ──────────────────────────────
    public static void runPageScrollTest(String targetUrl) {
        String testName = "Page Scroll Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long pageHeight = (Long) js.executeScript("return document.body.scrollHeight");
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(1000);
            js.executeScript("window.scrollTo(0, 0)");
            if (pageHeight <= 0) {
                status = "FAIL"; errorMessage = "Page has no scrollable content";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Page scrollable. Height: " + pageHeight + "px"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "INFO", targetUrl);
    }

    // ─── TEST 11: Console Errors — CRITICAL ──────────────────────
    public static void runConsoleErrorTest(String targetUrl) {
        String testName = "Console Error Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            ChromeOptions options = new ChromeOptions();
            LoggingPreferences prefs = new LoggingPreferences();
            prefs.enable(LogType.BROWSER, Level.ALL);
            options.setCapability("goog:loggingPrefs", prefs);
            driver = new ChromeDriver(options);
            driver.get(targetUrl);
            Thread.sleep(2000);
            var logs = driver.manage().logs().get(LogType.BROWSER);
            long errorCount = logs.getAll().stream().filter(log -> log.getLevel().equals(Level.SEVERE)).count();
            if (errorCount > 0) {
                status = "FAIL"; errorMessage = "Found " + errorCount + " console errors";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ No console errors"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "CRITICAL", targetUrl);
    }

    // ─── TEST 12: JavaScript Errors — CRITICAL ───────────────────
    public static void runJavaScriptErrorTest(String targetUrl) {
        String testName = "JavaScript Error Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            Boolean jqueryFailed = (Boolean) ((JavascriptExecutor) driver).executeScript(
                "return typeof $ === 'undefined' && document.querySelectorAll('script[src*=jquery]').length > 0");
            if (jqueryFailed != null && jqueryFailed) {
                status = "FAIL"; errorMessage = "jQuery failed to load despite being referenced";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ No major JS errors"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "CRITICAL", targetUrl);
    }

    // ─── TEST 13: Cookie — INFO ───────────────────────────────────
    public static void runCookieTest(String targetUrl) {
        String testName = "Cookie Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            var cookies = driver.manage().getCookies();
            System.out.println("🍪 Found " + cookies.size() + " cookies");
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "INFO", targetUrl);
    }

    // ─── TEST 14: 404 — WARNING ───────────────────────────────────
    public static void run404ErrorPageTest(String targetUrl) {
        String testName = "404 Error Page Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            String notFoundUrl = targetUrl.replaceAll("/$", "") + "/this-page-does-not-exist-xyz-123";
            driver.get(notFoundUrl);
            Thread.sleep(1000);
            String pageSource = driver.getPageSource().toLowerCase();
            String title = driver.getTitle().toLowerCase();
            String finalUrl = driver.getCurrentUrl();
            boolean has404 = pageSource.contains("404") || pageSource.contains("not found")
                || title.contains("404") || title.contains("not found");
            boolean redirectedToHome = finalUrl.equals(targetUrl) || finalUrl.equals(targetUrl + "/");
            if (!has404 && !redirectedToHome) {
                status = "FAIL"; errorMessage = "Site does not handle 404 pages properly";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ 404 handling OK"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "WARNING", targetUrl);
    }

    // ─── TEST 15: Accessibility — WARNING ────────────────────────
    public static void runAccessibilityTest(String targetUrl) {
        String testName = "Accessibility Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            List<WebElement> images = driver.findElements(By.tagName("img"));
            long missingAlt = images.stream()
                .filter(img -> { String alt = img.getAttribute("alt"); return alt == null || alt.isEmpty(); }).count();
            List<WebElement> buttons = driver.findElements(By.tagName("button"));
            long missingAria = buttons.stream()
                .filter(btn -> {
                    String aria = btn.getAttribute("aria-label"); String text = btn.getText();
                    return (aria == null || aria.isEmpty()) && (text == null || text.isEmpty());
                }).count();
            WebElement html = driver.findElement(By.tagName("html"));
            boolean missingLang = html.getAttribute("lang") == null || html.getAttribute("lang").isEmpty();
            if (missingLang && missingAlt > 3) {
                status = "FAIL";
                errorMessage = missingAlt + " images missing alt + html missing lang attribute";
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Accessibility OK (minor warnings only)"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "WARNING", targetUrl);
    }

    // ─── TEST 16: Performance — WARNING ──────────────────────────
    public static void runPerformanceMetricsTest(String targetUrl) {
        String testName = "Performance Metrics Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            Thread.sleep(3000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long domLoad = (Long) js.executeScript(
                "return window.performance.timing.domContentLoadedEventEnd - window.performance.timing.navigationStart");
            Long fullLoad = (Long) js.executeScript(
                "return window.performance.timing.loadEventEnd - window.performance.timing.navigationStart");
            System.out.println("📊 DOM: " + domLoad + "ms, Full: " + fullLoad + "ms");
            StringBuilder issues = new StringBuilder();
            if (domLoad > 3000) issues.append("DOM load slow: " + domLoad + "ms, ");
            if (fullLoad > 5000) issues.append("Full load slow: " + fullLoad + "ms, ");
            if (issues.length() > 0) {
                status = "FAIL"; errorMessage = "Performance: " + issues.toString().replaceAll(", $", "");
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ Performance OK"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "WARNING", targetUrl);
    }

    // ─── TEST 17: Duplicate Content — INFO ───────────────────────
    public static void runDuplicateContentTest(String targetUrl) {
        String testName = "Duplicate Content Test";
        WebDriver driver = null;
        String status = "PASS"; String errorMessage = ""; String screenshotPath = "";
        try {
            driver = createDriver();
            driver.get(targetUrl);
            List<WebElement> titles = driver.findElements(By.tagName("title"));
            List<WebElement> h1s = driver.findElements(By.tagName("h1"));
            StringBuilder issues = new StringBuilder();
            if (titles.size() > 1) issues.append("Multiple title tags (" + titles.size() + "), ");
            if (h1s.size() > 1) issues.append("Multiple H1 tags (" + h1s.size() + "), ");
            if (issues.length() > 0) {
                status = "FAIL"; errorMessage = "Duplicate content: " + issues.toString().replaceAll(", $", "");
                screenshotPath = takeScreenshot(driver, testName);
            } else { System.out.println("✅ No duplicate content issues"); }
        } catch (Exception e) { status = "FAIL"; errorMessage = e.getMessage(); screenshotPath = takeScreenshot(driver, testName); }
        finally { if (driver != null) driver.quit(); }
        sendToBackend(testName, status, errorMessage, screenshotPath, "INFO", targetUrl);
    }

    // ─── Helpers ─────────────────────────────────────────────────
   private static WebDriver createDriver() {
    WebDriverManager.chromedriver().setup();

    ChromeOptions options = new ChromeOptions();
    options.addArguments("--remote-allow-origins=*");
    options.addArguments("--disable-dev-shm-usage");
    options.addArguments("--no-sandbox");

    return new ChromeDriver(options);
}

    public static String takeScreenshot(WebDriver driver, String testName) {
        try {
            if (driver == null) return "";
            File dir = new File("screenshots");
            if (!dir.exists()) dir.mkdirs();
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            String path = "screenshots/" + testName.replace(" ", "_") + "_" + System.currentTimeMillis() + ".png";
            FileUtils.copyFile(src, new File(path));
            return path;
        } catch (Exception e) { System.err.println("Screenshot failed: " + e.getMessage()); return ""; }
    }

    // ✅ Updated: includes targetUrl as testSuite
  private static void sendToBackend(String testName, String status,
                                  String errorMessage, String screenshotPath,
                                  String severity, String targetUrl) {

    try {
        URL url = new URL("http://localhost:8081/api/test/run");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);

        // ✅ FIXED (important)
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept", "application/json");

        Map<String, String> data = new HashMap<>();
        data.put("testName", testName);
        data.put("status", status);
        data.put("errorMessage", errorMessage);
        data.put("screenshotPath", screenshotPath);
        data.put("severity", severity);
        data.put("testSuite", targetUrl.toLowerCase());

        String json = new ObjectMapper().writeValueAsString(data);

        System.out.println("📦 SEND → " + json);

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        os.flush();
        os.close();

        int code = conn.getResponseCode();
        System.out.println("📤 RESPONSE → " + code);

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}