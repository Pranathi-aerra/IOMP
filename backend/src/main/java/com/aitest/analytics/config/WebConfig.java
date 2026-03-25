package com.aitest.analytics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Serves the local 'screenshots/' directory as static files at /screenshots/**
 * so the frontend can load images via http://localhost:8081/screenshots/Test_12345.png
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve to absolute path of the screenshots folder (relative to working dir)
        String screenshotsPath = Paths.get("screenshots").toAbsolutePath().toUri().toString();
        if (!screenshotsPath.endsWith("/")) screenshotsPath += "/";

        registry.addResourceHandler("/screenshots/**")
                .addResourceLocations(screenshotsPath)
                .setCachePeriod(0); // no caching during dev
    }
}
