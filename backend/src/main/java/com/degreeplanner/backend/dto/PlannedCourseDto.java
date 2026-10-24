package com.degreeplanner.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlannedCourseDto(
        Long courseId,
        String code,
        String title,
        BigDecimal credits,
        String prereqText,
        List<String> missingPrereqs,
        String status) {}