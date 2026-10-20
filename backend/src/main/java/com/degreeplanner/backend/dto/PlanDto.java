package com.degreeplanner.backend.dto;

import java.util.List;

public record PlanDto(int startYear, String startTerm, List<SemesterDto> semesters) {}