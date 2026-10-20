package com.degreeplanner.backend.planner;

import static org.assertj.core.api.Assertions.assertThat;

import com.degreeplanner.backend.dto.CourseDto;
import com.degreeplanner.backend.dto.RequirementGroupDto;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ProgressCalculatorTest {

    private static CourseDto course(String code, int credits) {
        return new CourseDto(null, code, code, BigDecimal.valueOf(credits), null);
    }

    private static RequirementGroupDto group(String rule, Integer minCourses, Integer minCredits,
                                             List<CourseDto> courses, List<RequirementGroupDto> children) {
        return new RequirementGroupDto(1L, "Test group", rule, minCourses,
                minCredits == null ? null : BigDecimal.valueOf(minCredits), courses, children);
    }

    @Test
    void allGroupNeedsEveryCourse() {
        RequirementGroupDto g = group("ALL", null, null,
                List.of(course("CS 110", 3), course("CS 211", 3)), List.of());

        GroupProgress partial = ProgressCalculator.evaluate(g, Set.of("CS 110"));
        assertThat(partial.done()).isFalse();
        assertThat(partial.completedCourses()).containsExactly("CS 110");
        assertThat(partial.remainingCourses()).containsExactly("CS 211");

        assertThat(ProgressCalculator.evaluate(g, Set.of("CS 110", "CS 211")).done()).isTrue();
    }

    @Test
    void chooseNNeedsEnoughCourses() {
        RequirementGroupDto g = group("CHOOSE_N", 2, null,
                List.of(course("A 1", 3), course("A 2", 3), course("A 3", 3), course("A 4", 3)), List.of());

        assertThat(ProgressCalculator.evaluate(g, Set.of("A 1")).done()).isFalse();
        assertThat(ProgressCalculator.evaluate(g, Set.of("A 1", "A 4")).done()).isTrue();
    }

    @Test
    void minCreditsAddsUpCredits() {
        RequirementGroupDto g = group("MIN_CREDITS", null, 6,
                List.of(course("B 1", 3), course("B 2", 3), course("B 3", 4)), List.of());

        assertThat(ProgressCalculator.evaluate(g, Set.of("B 1")).done()).isFalse();
        assertThat(ProgressCalculator.evaluate(g, Set.of("B 1", "B 3")).done()).isTrue();
    }

    @Test
    void parentIsNotDoneUntilChildIsDone() {
        RequirementGroupDto child = group("CHOOSE_N", 1, null, List.of(course("C 1", 3)), List.of());
        RequirementGroupDto parent = group("CHOOSE_N", 2, null,
                List.of(course("C 1", 3), course("C 2", 3), course("C 3", 3)), List.of(child));

        assertThat(ProgressCalculator.evaluate(parent, Set.of("C 2", "C 3")).done()).isFalse();
        assertThat(ProgressCalculator.evaluate(parent, Set.of("C 1", "C 2")).done()).isTrue();
    }

    @Test
    void groupWithOnlyChildrenIsDoneWhenChildrenAre() {
        RequirementGroupDto child = group("ALL", null, null, List.of(course("D 1", 3)), List.of());
        RequirementGroupDto parent = group("ALL", null, null, List.of(), List.of(child));

        assertThat(ProgressCalculator.evaluate(parent, Set.of()).done()).isFalse();
        assertThat(ProgressCalculator.evaluate(parent, Set.of("D 1")).done()).isTrue();
    }
}