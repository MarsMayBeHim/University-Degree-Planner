package com.degreeplanner.backend.planner;

import static com.degreeplanner.backend.planner.PrereqNode.course;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class PlanGeneratorTest {

    private static PlanCourse c(String code, PrereqNode tree) {
        return new PlanCourse(1L, code, 3, tree, Set.of());
    }

    private static PlanGenerator.Settings settings(int semesters, int maxCredits) {
        return new PlanGenerator.Settings(2025, "FALL", semesters, maxCredits);
    }

    private static int semesterOf(PlanGenerator.Result r, String code) {
        return r.placements().stream()
                .filter(p -> p.code().equals(code))
                .findFirst().orElseThrow().semesterIndex();
    }

    @Test
    void prerequisitesLandInEarlierSemesters() {
        var result = PlanGenerator.generate(
                List.of(c("A 1", null), c("B 1", course("A 1"))), Set.of(), settings(8, 12));
        assertThat(semesterOf(result, "A 1")).isEqualTo(0);
        assertThat(semesterOf(result, "B 1")).isEqualTo(1);
    }

    @Test
    void semestersBelowTheMinimumAreFlaggedExceptTheLastOne() {
        // B 1 needs A 1, so semester 0 holds only A 1 (3 credits) and semester 1 only B 1
        var result = PlanGenerator.generate(
                List.of(c("A 1", null), c("B 1", course("A 1"))), Set.of(),
                new PlanGenerator.Settings(2025, "FALL", 8, 12, 18));

        assertThat(result.lightSemesters()).hasSize(1);
        assertThat(result.lightSemesters().get(0).credits()).isEqualTo(3);
    }

    @Test
    void noMinimumMeansNothingIsFlagged() {
        var result = PlanGenerator.generate(
                List.of(c("A 1", null), c("B 1", course("A 1"))), Set.of(), settings(8, 18));

        assertThat(result.lightSemesters()).isEmpty();
    }

    @Test
    void semesterTotalIsMeasuredOncePerSemester() {
        // Semester 0 holds five 3-credit courses (15 credits). F 1 needs A 1, so it lands in semester 1.
        var result = PlanGenerator.generate(
                List.of(c("A 1", null), c("B 1", null), c("C 1", null),
                        c("D 1", null), c("E 1", null), c("F 1", course("A 1"))),
                Set.of(), new PlanGenerator.Settings(2025, "FALL", 8, 12, 18));

        assertThat(result.lightSemesters()).isEmpty();
    }

    @Test
    void lowerLevelCoursesComeFirstWhenNothingElseDecides() {
        var result = PlanGenerator.generate(
                List.of(c("HI 489", null), c("HAP 201", null)), Set.of(), settings(8, 3));
        assertThat(semesterOf(result, "HAP 201")).isEqualTo(0);
        assertThat(semesterOf(result, "HI 489")).isEqualTo(1);
    }
    @Test
    void creditLimitIsRespected() {
        var result = PlanGenerator.generate(
                List.of(c("A 1", null), c("B 1", null), c("C 1", null)), Set.of(), settings(8, 6));
        long inFirst = result.placements().stream().filter(p -> p.semesterIndex() == 0).count();
        assertThat(inFirst).isEqualTo(2);
        assertThat(semesterOf(result, "C 1")).isEqualTo(1);
    }

    @Test
    void completedCoursesSatisfyPrerequisites() {
        var result = PlanGenerator.generate(
                List.of(c("B 1", course("A 1"))), Set.of("A 1"), settings(8, 12));
        assertThat(semesterOf(result, "B 1")).isEqualTo(0);
    }

    @Test
    void longestChainIsScheduledFirst() {
        // With room for one course per semester, Z starts a three-course chain and A is standalone
        var result = PlanGenerator.generate(
                List.of(c("A 1", null), c("Z 1", null),
                        c("Y 1", course("Z 1")), c("X 1", course("Y 1"))),
                Set.of(), settings(8, 3));
        assertThat(semesterOf(result, "Z 1")).isEqualTo(0);
    }

    @Test
    void termRestrictionIsRespected() {
        PlanCourse springOnly = new PlanCourse(1L, "S 1", 3, null, Set.of("SPRING"));
        var result = PlanGenerator.generate(List.of(springOnly), Set.of(), settings(8, 12));
        var placement = result.placements().get(0);
        assertThat(placement.term()).isEqualTo("SPRING");
        assertThat(placement.year()).isEqualTo(2026);
    }

    @Test
    void coursesThatDoNotFitAreReportedAsUnplaced() {
        var result = PlanGenerator.generate(
                List.of(c("A 1", null), c("B 1", null), c("C 1", null)), Set.of(), settings(1, 6));
        assertThat(result.placements()).hasSize(2);
        assertThat(result.unplaced()).hasSize(1);
    }

    @Test
    void prerequisiteCycleIsDetected() {
        assertThatThrownBy(() -> PlanGenerator.generate(
                List.of(c("A 1", course("B 1")), c("B 1", course("A 1"))), Set.of(), settings(8, 12)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cycle");
    }
}