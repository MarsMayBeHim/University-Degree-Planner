package com.degreeplanner.backend.dto;

import com.degreeplanner.backend.model.Course;
import java.math.BigDecimal;

public record CourseDto(Long id, String code, String title, BigDecimal credits, String prereqText) {
    public static CourseDto from(Course c) {
        return new CourseDto(c.getId(), c.getSubject() + " " + c.getNumber(),
                c.getTitle(), c.getCreditsMin(), c.getPrereqText());
    }
}