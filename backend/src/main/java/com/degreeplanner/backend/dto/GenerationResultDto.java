package com.degreeplanner.backend.dto;

import java.util.List;

public record GenerationResultDto(
        int coursesPlaced,
        int semestersUsed,
        String startLabel,
        List<Unplaced> unplaced,
        List<LightSemester> lightSemesters) {

    public record Unplaced(String code, String reason) {}

    public record LightSemester(String label, int credits) {}
}