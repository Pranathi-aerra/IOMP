package com.aitest.analytics.repository;

import com.aitest.analytics.model.AIRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AIRuleRepository extends MongoRepository<AIRule, String> {

    Optional<AIRule> findByKeyword(String keyword);
    List<AIRule> findByCategory(String category);
    List<AIRule> findBySource(String source);
    boolean existsByKeyword(String keyword);
    List<AIRule> findByKeywordContainingIgnoreCase(String keyword);
}