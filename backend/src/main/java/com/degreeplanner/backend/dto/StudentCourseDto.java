package com.degreeplanner.backend.dto;

import java.math.BigDecimal;

public record StudentCourseDto(
        Long courseId,
        String code,
        String title,
        BigDecimal credits,
        String status,
        Integer plannedYear,
        String plannedTerm) {}