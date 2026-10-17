package com.degreeplanner.backend.dto;

import java.math.BigDecimal;

public record CourseDto(Long id, String code, String title, BigDecimal credits, String prereqText) {}