package com.degreeplanner.backend.dto;

import java.util.List;

public record GenerationResultDto(int coursesPlaced, int semestersUsed, List<Unplaced> unplaced) {

    public record Unplaced(String code, String reason) {}
}