package com.degreeplanner.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ProfileRequest(
        @NotNull Long programId,
        @NotNull @Min(2000) @Max(2100) Integer startYear,
        @NotNull @Pattern(regexp = "FALL|SPRING") String startTerm) {}