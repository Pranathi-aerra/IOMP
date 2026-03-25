package com.aitest.analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing // ✅ enables @CreatedDate to auto-populate
public class AiTestFailureApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiTestFailureApplication.class, args);
    }
}