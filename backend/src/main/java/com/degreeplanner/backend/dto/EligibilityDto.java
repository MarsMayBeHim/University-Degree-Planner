package com.degreeplanner.backend.dto;

import java.util.List;

public record EligibilityDto(
        Long courseId,
        String code,
        boolean eligible,
        String prereqText,
        List<String> unmetCourses) {}