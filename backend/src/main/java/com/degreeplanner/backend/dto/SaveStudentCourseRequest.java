package com.degreeplanner.backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record SaveStudentCourseRequest(
        @NotNull Long courseId,
        @NotNull @Pattern(regexp = "COMPLETED|IN_PROGRESS|PLANNED") String status,
        Integer plannedYear,
        String plannedTerm,
        Boolean clearSchedule) {}