package com.degreeplanner.backend.dto;

import java.math.BigDecimal;
import java.util.List;

public record RequirementGroupDto(
        Long id,
        String name,
        String ruleType,
        Integer minCourses,
        BigDecimal minCredits,
        List<CourseDto> courses,
        List<RequirementGroupDto> children) {}