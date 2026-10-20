package com.degreeplanner.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record SemesterDto(
        int year,
        String term,
        String label,
        BigDecimal credits,
        List<PlannedCourseDto> courses) {}