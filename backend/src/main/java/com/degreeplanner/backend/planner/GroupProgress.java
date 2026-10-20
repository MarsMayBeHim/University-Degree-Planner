package com.degreeplanner.backend.planner;

import java.math.BigDecimal;
import java.util.List;

public record GroupProgress(
        Long id,
        String name,
        String ruleType,
        boolean done,
        int coursesCompleted,
        Integer coursesRequired,
        BigDecimal creditsEarned,
        BigDecimal creditsRequired,
        List<String> completedCourses,
        List<String> remainingCourses,
        List<GroupProgress> children) {}