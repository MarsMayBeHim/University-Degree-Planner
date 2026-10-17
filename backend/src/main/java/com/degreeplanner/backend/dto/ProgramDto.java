package com.degreeplanner.backend.dto;

public record ProgramDto(Long id, String name, String degreeType, String catalogYear, int totalCredits) {}