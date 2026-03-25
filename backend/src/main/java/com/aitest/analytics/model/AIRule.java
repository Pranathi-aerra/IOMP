package com.aitest.analytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "aiRules")
public class AIRule {

    @Id
    private String id;

    @Indexed(unique = true) // ✅ No duplicate keywords
    private String keyword;

    private String category;
    private String suggestion;
    private String source; // "MANUAL" or "AI_GENERATED"

    @CreatedDate
    private LocalDateTime createdAt;

    public AIRule() {
        this.source = "MANUAL";
        this.createdAt = LocalDateTime.now();
    }

    public AIRule(String keyword, String category, String suggestion, String source) {
        this.keyword = keyword;
        this.category = category;
        this.suggestion = suggestion;
        this.source = source;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "AIRule{id='" + id + "', keyword='" + keyword + "', category='" + category
            + "', source='" + source + "', createdAt=" + createdAt + '}';
    }
}