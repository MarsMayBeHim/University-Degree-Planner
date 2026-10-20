package com.degreeplanner.backend.dto;

import com.degreeplanner.backend.planner.GroupProgress;
import java.math.BigDecimal;
import java.util.List;

public record ProgressDto(
        Long programId,
        String programName,
        String catalogYear,
        int totalCreditsRequired,
        BigDecimal creditsCompleted,
        BigDecimal creditsPlanned,
        boolean trackedRequirementsMet,
        List<GroupProgress> groups) {}