package com.degreeplanner.backend.dto;

import com.degreeplanner.backend.planner.PrereqNode;
import java.math.BigDecimal;

public record PrereqCourseDto(
        Long id,
        String code,
        String title,
        BigDecimal credits,
        String prereqText,
        PrereqNode prereqTree) {}