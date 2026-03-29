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
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * SeleniumRunner — single shared headless driver, 25 tests total.
 *
 * Original 17 tests + 8 new:
 *   18. HTTPS Enforcement     (Security)
 *   19. Security Headers      (Security)
 *   20. Mobile Viewport       (Responsiveness)
 *   21. SEO Heading Structure (SEO)
 *   22. Open Graph Tags       (SEO)
 *   23. Form Validation       (Functional)
 *   24. Inline Script Check   (Security)
 *   25. Social Links Check    (Content)
 */
public class SeleniumRunner {

    private static final String BACKEND_URL = "http://localhost:8081";
    private static final ThreadLocal<String> currentUserEmail = new ThreadLocal<>();

    private static String now() {
        return java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    private static void log(String msg) { System.out.println(now() + " | " + msg); }

    // ── Entry point ────────────────────────────────────────────────────────────
    public static void runAllTestsOnUrl(String targetUrl, String userEmail) {
        currentUserEmail.set(userEmail);
        log("🚀 Starting headless test suite (25 tests) on: " + targetUrl);
        WebDriver driver = null;
        try {
            driver = createHeadlessDriver();
            log("✅ Headless Chrome started");

            // ── Original 17 ──
            runPageLoadTest(driver, targetUrl);
            runTitleVerificationTest(driver, targetUrl);
            runElementPresenceTest(driver, targetUrl);
            runButtonClickTest(driver, targetUrl);
            runFormSubmissionTest(driver, targetUrl);
            runBrokenLinksTest(driver, targetUrl);
            runScreenshotTest(driver, targetUrl);
            runMetaTagsTest(driver, targetUrl);
            runImageLoadingTest(driver, targetUrl);
            runPageScrollTest(driver, targetUrl);
            runConsoleErrorTest(driver, targetUrl);   // uses own logging driver
            runJavaScriptErrorTest(driver, targetUrl);
            runCookieTest(driver, targetUrl);
            run404ErrorPageTest(driver, targetUrl);
            runAccessibilityTest(driver, targetUrl);
            runPerformanceMetricsTest(driver, targetUrl);
            runDuplicateContentTest(driver, targetUrl);

            // ── 8 New Tests ──
            runHttpsEnforcementTest(driver, targetUrl);
            runSecurityHeadersTest(driver, targetUrl);
            runMobileViewportTest(driver, targetUrl);
            runSeoHeadingStructureTest(driver, targetUrl);
            runOpenGraphTagsTest(driver, targetUrl);
            runFormValidationTest(driver, targetUrl);
            runInlineScriptTest(driver, targetUrl);
            runSocialLinksTest(driver, targetUrl);

            log("✅ All 25 tests completed for: " + targetUrl);
        } catch (Exception e) {
            log("❌ Fatal error during test suite: " + e.getMessage());
        } finally {
            if (driver != null) { driver.quit(); log("🔒 Browser closed"); }
            currentUserEmail.remove();
        }
    }

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : "https://example.com";
        runAllTestsOnUrl(url, null);
    }

    // ── Driver factories ───────────────────────────────────────────────────────
    private static WebDriver createHeadlessDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080",
                       "--no-sandbox", "--disable-dev-shm-usage",
                       "--disable-notifications", "--remote-allow-origins=*");
        return new ChromeDriver(o);
    }

    /** Special driver with browser-log capture (needed for Console Error test). */
    private static WebDriver createLoggingDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions o = new ChromeOptions();
        o.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080",
                       "--no-sandbox", "--disable-dev-shm-usage", "--remote-allow-origins=*");
        LoggingPreferences prefs = new LoggingPreferences();
        prefs.enable(LogType.BROWSER, Level.ALL);
        o.setCapability("goog:loggingPrefs", prefs);
        return new ChromeDriver(o);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ORIGINAL 17 TESTS
    // ══════════════════════════════════════════════════════════════════════════

    // TEST 1: Page Load
    public static void runPageLoadTest(WebDriver driver, String targetUrl) {
        String name = "Page Load Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            long start = System.currentTimeMillis();
            driver.get(targetUrl);
            long t = System.currentTimeMillis() - start;
            if (t > 5000) { status = "FAIL"; err = "Page load too slow: " + t + "ms (threshold: 5000ms)"; ss = shot(driver, name); }
            else log("✅ " + name + " — " + t + "ms");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 2: Title Verification
    public static void runTitleVerificationTest(WebDriver driver, String targetUrl) {
        String name = "Title Verification Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            String title = driver.getTitle();
            if (title == null || title.isEmpty()) { status = "FAIL"; err = "Page title is empty or missing"; ss = shot(driver, name); }
            else log("✅ " + name + " — " + title);
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 3: Element Presence
    public static void runElementPresenceTest(WebDriver driver, String targetUrl) {
        String name = "Element Presence Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            String[] tags = {"body", "header", "nav", "main", "footer"};
            int found = 0;
            for (String tag : tags) { try { driver.findElement(By.tagName(tag)); found++; } catch (Exception ignored) {} }
            if (found == 0) { status = "FAIL"; err = "No common structural elements found"; ss = shot(driver, name); }
            else log("✅ " + name + " — " + found + " elements");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 4: Button Click
    public static void runButtonClickTest(WebDriver driver, String targetUrl) {
        String name = "Button Click Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> btns = driver.findElements(By.tagName("button"));
            if (btns.isEmpty()) btns = driver.findElements(By.cssSelector("input[type='button'],input[type='submit']"));
            if (btns.isEmpty()) { status = "FAIL"; err = "No clickable buttons found"; ss = shot(driver, name); }
            else log("✅ " + name + " — " + btns.size() + " buttons");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 5: Form Submission
    public static void runFormSubmissionTest(WebDriver driver, String targetUrl) {
        String name = "Form Submission Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            List<WebElement> inputs = driver.findElements(By.tagName("input"));
            if (forms.isEmpty() && inputs.isEmpty()) { status = "FAIL"; err = "No forms or inputs found"; ss = shot(driver, name); }
            else log("✅ " + name + " — " + forms.size() + " forms, " + inputs.size() + " inputs");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 6: Broken Links
    public static void runBrokenLinksTest(WebDriver driver, String targetUrl) {
        String name = "Broken Links Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> links = driver.findElements(By.tagName("a"));
            int broken = 0, checked = 0;
            for (WebElement link : links) {
                if (checked >= 10) break;
                String href = link.getAttribute("href");
                if (href == null || href.startsWith("#") || href.startsWith("javascript")) continue;
                try {
                    URL url = new URL(href);
                    HttpURLConnection c = (HttpURLConnection) url.openConnection();
                    c.setRequestMethod("HEAD"); c.setConnectTimeout(3000); c.setReadTimeout(3000);
                    if (c.getResponseCode() >= 400) broken++;
                    checked++;
                } catch (Exception ignored) {}
            }
            if (broken > 0) { status = "FAIL"; err = broken + " broken links out of " + checked + " checked"; ss = shot(driver, name); }
            else log("✅ " + name + " — no broken links in " + checked);
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 7: Screenshot Capture
    public static void runScreenshotTest(WebDriver driver, String targetUrl) {
        String name = "Screenshot Capture Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            Thread.sleep(1000);
            ss = shot(driver, name);
            if (ss.isEmpty()) { status = "FAIL"; err = "Failed to capture screenshot"; }
            else log("✅ " + name + " — " + ss);
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // TEST 8: Meta Tags
    public static void runMetaTagsTest(WebDriver driver, String targetUrl) {
        String name = "Meta Tags Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> metas = driver.findElements(By.tagName("meta"));
            boolean hasDesc = false, hasVp = false, hasCharset = false;
            for (WebElement m : metas) {
                String n = m.getAttribute("name"), c = m.getAttribute("content"), ch = m.getAttribute("charset");
                if ("description".equalsIgnoreCase(n) && c != null && !c.isEmpty()) hasDesc = true;
                if ("viewport".equalsIgnoreCase(n)) hasVp = true;
                if (ch != null && !ch.isEmpty()) hasCharset = true;
            }
            StringBuilder missing = new StringBuilder();
            if (!hasDesc) missing.append("meta description, ");
            if (!hasVp)   missing.append("viewport, ");
            if (!hasCharset) missing.append("charset, ");
            if (missing.length() > 0) { status = "FAIL"; err = "Missing: " + missing.toString().replaceAll(", $", ""); ss = shot(driver, name); }
            else log("✅ " + name + " — all present");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // TEST 9: Image Loading
    public static void runImageLoadingTest(WebDriver driver, String targetUrl) {
        String name = "Image Loading Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> imgs = driver.findElements(By.tagName("img"));
            int broken = 0, missingAlt = 0;
            for (WebElement img : imgs) {
                Boolean loaded = (Boolean)((JavascriptExecutor)driver)
                    .executeScript("return arguments[0].complete && arguments[0].naturalWidth > 0", img);
                if (!loaded) broken++;
                String alt = img.getAttribute("alt");
                if (alt == null || alt.isEmpty()) missingAlt++;
            }
            if (broken > 0) { status = "FAIL"; err = broken + " broken images out of " + imgs.size() + " (" + missingAlt + " missing alt)"; ss = shot(driver, name); }
            else log("✅ " + name + " — " + imgs.size() + " images OK");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 10: Page Scroll
    public static void runPageScrollTest(WebDriver driver, String targetUrl) {
        String name = "Page Scroll Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long h = (Long) js.executeScript("return document.body.scrollHeight");
            js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
            Thread.sleep(500);
            js.executeScript("window.scrollTo(0, 0)");
            if (h <= 0) { status = "FAIL"; err = "Page has no scrollable content"; ss = shot(driver, name); }
            else log("✅ " + name + " — height: " + h + "px");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // TEST 11: Console Errors (own logging driver)
    public static void runConsoleErrorTest(WebDriver shared, String targetUrl) {
        String name = "Console Error Test";
        String status = "PASS"; String err = ""; String ss = "";
        WebDriver ld = null;
        try {
            ld = createLoggingDriver();
            ld.get(targetUrl);
            Thread.sleep(2000);
            long count = ld.manage().logs().get(LogType.BROWSER).getAll().stream()
                .filter(e -> e.getLevel().equals(Level.SEVERE)).count();
            if (count > 0) { status = "FAIL"; err = "Found " + count + " console errors"; ss = shot(ld, name); }
            else log("✅ " + name + " — no errors");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); }
        finally { if (ld != null) ld.quit(); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 12: JavaScript Errors
    public static void runJavaScriptErrorTest(WebDriver driver, String targetUrl) {
        String name = "JavaScript Error Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            Boolean jqFailed = (Boolean)((JavascriptExecutor)driver).executeScript(
                "return typeof $ === 'undefined' && document.querySelectorAll('script[src*=jquery]').length > 0");
            if (jqFailed != null && jqFailed) { status = "FAIL"; err = "jQuery failed to load despite being referenced"; ss = shot(driver, name); }
            else log("✅ " + name + " — OK");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 13: Cookie
    public static void runCookieTest(WebDriver driver, String targetUrl) {
        String name = "Cookie Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            var cookies = driver.manage().getCookies();
            log("✅ " + name + " — " + cookies.size() + " cookies");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // TEST 14: 404 Error Page
    public static void run404ErrorPageTest(WebDriver driver, String targetUrl) {
        String name = "404 Error Page Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl.replaceAll("/$", "") + "/this-page-does-not-exist-xyz-999");
            Thread.sleep(1000);
            String src = driver.getPageSource().toLowerCase(), title = driver.getTitle().toLowerCase(), final_ = driver.getCurrentUrl();
            boolean has404 = src.contains("404") || src.contains("not found") || title.contains("404");
            boolean redir  = final_.equals(targetUrl) || final_.equals(targetUrl + "/");
            if (!has404 && !redir) { status = "FAIL"; err = "Site does not handle 404 pages properly"; ss = shot(driver, name); }
            else log("✅ " + name + " — 404 handled");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 15: Accessibility
    public static void runAccessibilityTest(WebDriver driver, String targetUrl) {
        String name = "Accessibility Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            long missingAlt = driver.findElements(By.tagName("img")).stream()
                .filter(i -> { String a = i.getAttribute("alt"); return a == null || a.isEmpty(); }).count();
            boolean missingLang = driver.findElement(By.tagName("html")).getAttribute("lang") == null
                || driver.findElement(By.tagName("html")).getAttribute("lang").isEmpty();
            if (missingLang && missingAlt > 3) { status = "FAIL"; err = missingAlt + " imgs missing alt + html missing lang"; ss = shot(driver, name); }
            else log("✅ " + name + " — OK");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 16: Performance Metrics
    public static void runPerformanceMetricsTest(WebDriver driver, String targetUrl) {
        String name = "Performance Metrics Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            Thread.sleep(2000);
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Long dom  = (Long) js.executeScript("return window.performance.timing.domContentLoadedEventEnd - window.performance.timing.navigationStart");
            Long full = (Long) js.executeScript("return window.performance.timing.loadEventEnd - window.performance.timing.navigationStart");
            log("📊 " + name + " — DOM: " + dom + "ms, Full: " + full + "ms");
            StringBuilder issues = new StringBuilder();
            if (dom  > 3000) issues.append("DOM slow: " + dom + "ms, ");
            if (full > 5000) issues.append("Full load slow: " + full + "ms, ");
            if (issues.length() > 0) { status = "FAIL"; err = issues.toString().replaceAll(", $", ""); ss = shot(driver, name); }
            else log("✅ " + name + " — OK");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 17: Duplicate Content
    public static void runDuplicateContentTest(WebDriver driver, String targetUrl) {
        String name = "Duplicate Content Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            int titles = driver.findElements(By.tagName("title")).size();
            int h1s    = driver.findElements(By.tagName("h1")).size();
            StringBuilder issues = new StringBuilder();
            if (titles > 1) issues.append("Multiple <title> tags (" + titles + "), ");
            if (h1s    > 1) issues.append("Multiple <h1> tags (" + h1s + "), ");
            if (issues.length() > 0) { status = "FAIL"; err = issues.toString().replaceAll(", $", ""); ss = shot(driver, name); }
            else log("✅ " + name + " — OK");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  8 NEW TESTS
    // ══════════════════════════════════════════════════════════════════════════

    // TEST 18: HTTPS Enforcement
    public static void runHttpsEnforcementTest(WebDriver driver, String targetUrl) {
        String name = "HTTPS Enforcement Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.startsWith("https://")) {
                status = "FAIL";
                err = "Site is not served over HTTPS. Current URL: " + currentUrl;
                ss = shot(driver, name);
            } else {
                // Also check that http:// redirects to https://
                if (targetUrl.startsWith("https://")) {
                    String httpUrl = "http://" + targetUrl.substring(8);
                    try {
                        URL url = new URL(httpUrl);
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setInstanceFollowRedirects(false);
                        conn.setConnectTimeout(5000); conn.setReadTimeout(5000);
                        int code = conn.getResponseCode();
                        if (code < 300 || code >= 400) {
                            status = "FAIL";
                            err = "HTTP does not redirect to HTTPS (response: " + code + ")";
                            ss = shot(driver, name);
                        } else log("✅ " + name + " — HTTPS enforced, HTTP redirects (" + code + ")");
                    } catch (Exception ignored) { log("✅ " + name + " — HTTPS OK (HTTP unreachable)"); }
                } else { log("✅ " + name + " — HTTPS OK"); }
            }
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 19: Security Headers
    public static void runSecurityHeadersTest(WebDriver driver, String targetUrl) {
        String name = "Security Headers Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            URL url = new URL(targetUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET"); conn.setConnectTimeout(5000); conn.setReadTimeout(5000);
            conn.connect();
            StringBuilder missing = new StringBuilder();
            String[] headers = {
                "Strict-Transport-Security", "X-Content-Type-Options",
                "X-Frame-Options", "Content-Security-Policy"
            };
            for (String h : headers) {
                if (conn.getHeaderField(h) == null) missing.append(h + ", ");
            }
            conn.disconnect();
            if (missing.length() > 0) {
                status = "FAIL";
                err = "Missing security headers: " + missing.toString().replaceAll(", $", "");
                ss = shot(driver, name);
            } else log("✅ " + name + " — all security headers present");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "CRITICAL", targetUrl);
    }

    // TEST 20: Mobile Viewport Responsiveness
    public static void runMobileViewportTest(WebDriver driver, String targetUrl) {
        String name = "Mobile Viewport Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            // Switch to mobile viewport
            ((JavascriptExecutor) driver).executeScript(
                "Object.defineProperty(window, 'innerWidth', {get: function(){return 375;}})");
            driver.manage().window().setSize(new Dimension(375, 812));
            driver.get(targetUrl);
            Thread.sleep(1000);

            // Check horizontal scroll (bad sign on mobile)
            Long scrollWidth  = (Long)((JavascriptExecutor)driver).executeScript("return document.body.scrollWidth");
            Long clientWidth  = (Long)((JavascriptExecutor)driver).executeScript("return document.documentElement.clientWidth");
            List<WebElement> metas = driver.findElements(By.cssSelector("meta[name='viewport']"));

            StringBuilder issues = new StringBuilder();
            if (metas.isEmpty()) issues.append("No viewport meta tag; ");
            if (scrollWidth != null && clientWidth != null && scrollWidth > clientWidth + 10) {
                issues.append("Horizontal scroll detected (scrollWidth=" + scrollWidth + " > clientWidth=" + clientWidth + "); ");
            }
            if (issues.length() > 0) { status = "FAIL"; err = issues.toString(); ss = shot(driver, name); }
            else log("✅ " + name + " — mobile-friendly");

            // Reset to desktop
            driver.manage().window().setSize(new Dimension(1920, 1080));
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 21: SEO Heading Structure
    public static void runSeoHeadingStructureTest(WebDriver driver, String targetUrl) {
        String name = "SEO Heading Structure Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> h1 = driver.findElements(By.tagName("h1"));
            List<WebElement> h2 = driver.findElements(By.tagName("h2"));
            List<WebElement> titles = driver.findElements(By.tagName("title"));

            StringBuilder issues = new StringBuilder();
            if (h1.isEmpty()) issues.append("No <h1> tag found; ");
            if (h1.size() > 1) issues.append("Multiple <h1> tags (" + h1.size() + ") — only one recommended; ");
            if (titles.isEmpty()) issues.append("No <title> tag found; ");
            if (h2.isEmpty()) issues.append("No <h2> tags found (flat heading hierarchy); ");

            // Check title length (SEO: 50-60 chars ideal)
            if (!titles.isEmpty()) {
                String titleText = titles.get(0).getAttribute("textContent");
                if (titleText != null) {
                    if (titleText.length() < 10) issues.append("Title too short (" + titleText.length() + " chars); ");
                    if (titleText.length() > 70) issues.append("Title too long (" + titleText.length() + " chars, >70); ");
                }
            }

            if (issues.length() > 0) { status = "FAIL"; err = issues.toString().replaceAll("; $", ""); ss = shot(driver, name); }
            else log("✅ " + name + " — " + h1.size() + " H1, " + h2.size() + " H2");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 22: Open Graph Tags
    public static void runOpenGraphTagsTest(WebDriver driver, String targetUrl) {
        String name = "Open Graph Tags Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            String[] ogProps = {"og:title", "og:description", "og:image", "og:url"};
            StringBuilder missing = new StringBuilder();
            for (String prop : ogProps) {
                List<WebElement> og = driver.findElements(By.cssSelector("meta[property='" + prop + "']"));
                if (og.isEmpty()) missing.append(prop + ", ");
            }
            if (missing.length() > 0) {
                status = "FAIL";
                err = "Missing Open Graph tags: " + missing.toString().replaceAll(", $", "");
                ss = shot(driver, name);
            } else log("✅ " + name + " — all OG tags present");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // TEST 23: Form Validation
    public static void runFormValidationTest(WebDriver driver, String targetUrl) {
        String name = "Form Validation Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            if (forms.isEmpty()) { log("✅ " + name + " — no forms to validate"); send(name, status, err, ss, "INFO", targetUrl); return; }

            StringBuilder issues = new StringBuilder();
            for (WebElement form : forms) {
                // Check for inputs missing required or label
                List<WebElement> inputs = form.findElements(By.cssSelector("input:not([type='hidden']):not([type='submit']):not([type='button'])"));
                for (WebElement input : inputs) {
                    String id     = input.getAttribute("id");
                    String req    = input.getAttribute("required");
                    String aria   = input.getAttribute("aria-label");
                    boolean hasLabel = id != null && !id.isEmpty()
                        && !driver.findElements(By.cssSelector("label[for='" + id + "']")).isEmpty();
                    if (!hasLabel && (aria == null || aria.isEmpty())) {
                        issues.append("Input missing label (id=" + id + "); ");
                    }
                }
            }
            if (issues.length() > 0) { status = "FAIL"; err = issues.toString().replaceAll("; $", ""); ss = shot(driver, name); }
            else log("✅ " + name + " — form validation OK");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "WARNING", targetUrl);
    }

    // TEST 24: Inline Script Detection (security risk)
    public static void runInlineScriptTest(WebDriver driver, String targetUrl) {
        String name = "Inline Script Security Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            // Count inline onclick/onmouseover etc. attributes — security risk
            Long inlineHandlers = (Long)((JavascriptExecutor)driver).executeScript(
                "var all = document.querySelectorAll('*'), c = 0;" +
                "for(var i=0;i<all.length;i++){" +
                "  var a=all[i].attributes;" +
                "  for(var j=0;j<a.length;j++){" +
                "    if(/^on[a-z]+$/.test(a[j].name))c++;" +
                "  }}" +
                "return c;");
            // Count inline <script> tags with content
            List<WebElement> inlineScripts = driver.findElements(By.cssSelector("script:not([src])"));
            long scriptCount = inlineScripts.stream()
                .filter(s -> { String t = s.getAttribute("textContent"); return t != null && !t.trim().isEmpty(); })
                .count();

            if (inlineHandlers != null && inlineHandlers > 10) {
                status = "FAIL";
                err = inlineHandlers + " inline event handlers (onclick etc.) detected — consider CSP";
                ss = shot(driver, name);
            } else log("✅ " + name + " — " + (inlineHandlers != null ? inlineHandlers : 0) + " inline handlers, " + scriptCount + " inline scripts");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // TEST 25: Social Links Check
    public static void runSocialLinksTest(WebDriver driver, String targetUrl) {
        String name = "Social Links Test";
        String status = "PASS"; String err = ""; String ss = "";
        try {
            driver.get(targetUrl);
            List<WebElement> links = driver.findElements(By.tagName("a"));
            String[] socialDomains = {"twitter.com", "x.com", "facebook.com", "instagram.com",
                                      "linkedin.com", "youtube.com", "github.com", "pinterest.com"};
            int found = 0;
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                if (href == null) continue;
                for (String domain : socialDomains) {
                    if (href.contains(domain)) { found++; break; }
                }
            }

            // Check if social links open in new tab
            int missingTarget = 0;
            for (WebElement link : links) {
                String href = link.getAttribute("href");
                if (href == null) continue;
                for (String domain : socialDomains) {
                    if (href.contains(domain)) {
                        String target = link.getAttribute("target");
                        if (!"_blank".equals(target)) missingTarget++;
                        break;
                    }
                }
            }

            if (found == 0) {
                status = "FAIL";
                err = "No social media links found on page";
                ss = shot(driver, name);
            } else if (missingTarget > 0) {
                status = "FAIL";
                err = found + " social links found but " + missingTarget + " don't open in new tab (missing target='_blank')";
                ss = shot(driver, name);
            } else log("✅ " + name + " — " + found + " social links, all open in new tab");
        } catch (Exception e) { status = "FAIL"; err = e.getMessage(); ss = shot(driver, name); }
        send(name, status, err, ss, "INFO", targetUrl);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    public static String shot(WebDriver driver, String testName) {
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

    // Kept for backward compat
    public static String takeScreenshot(WebDriver driver, String testName) { return shot(driver, testName); }

    private static void send(String testName, String status, String errorMessage,
                             String screenshotPath, String severity, String targetUrl) {
        try {
            URL url = new URL(BACKEND_URL + "/api/test/run");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST"); conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");

            Map<String, String> data = new HashMap<>();
            data.put("testName", testName);
            data.put("status", status);
            data.put("errorMessage", errorMessage);
            data.put("screenshotPath", screenshotPath);
            data.put("severity", severity);
            data.put("testSuite", targetUrl.toLowerCase());
            if (currentUserEmail.get() != null) {
                data.put("userEmail", currentUserEmail.get());
            }

            String json = new ObjectMapper().writeValueAsString(data);
            log("📦 → " + testName + " [" + status + "]");

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes()); os.flush(); os.close();
            int code = conn.getResponseCode();
            if (code != 200 && code != 201) log("⚠ Backend HTTP " + code + " for " + testName);
        } catch (Exception e) { System.err.println("send() failed: " + e.getMessage()); }
    }
}