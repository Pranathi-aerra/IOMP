package com.aitest.analytics.config;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class SeleniumConfig {

    @Bean
    @Scope("prototype") // ✅ new instance every time it's requested
    public WebDriver webDriver() {

        // ✅ Auto-manages chromedriver version
        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        // options.addArguments("--headless"); // uncomment for headless mode

        return new ChromeDriver(options);
    }
}