package com.degreeplanner.backend.planner;

import static com.degreeplanner.backend.planner.PrereqNode.course;
import static org.assertj.core.api.Assertions.assertThat;

import com.degreeplanner.backend.dto.CourseDto;
import com.degreeplanner.backend.dto.RequirementGroupDto;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class RequirementSelectorTest {

    private static RequirementGroupDto group(String rule, Integer minCourses, List<String> codes,
                                             List<RequirementGroupDto> children) {
        List<CourseDto> dtos = codes.stream()
                .map(code -> new CourseDto(null, code, code, BigDecimal.valueOf(3), null))
                .toList();
        return new RequirementGroupDto(1L, "g", rule, minCourses, null, dtos, children);
    }

    private static Map<String, PlanCourse> known(Map<String, PrereqNode> trees) {
        Map<String, PlanCourse> map = new LinkedHashMap<>();
        trees.forEach((code, tree) -> map.put(code, new PlanCourse(1L, code, 3, tree, Set.of())));
        return map;
    }

    @Test
    void allGroupSelectsEveryUncompletedCourse() {
        var g = group("ALL", null, List.of("A 1", "B 1"), List.of());
        Map<String, PrereqNode> trees = new LinkedHashMap<>();
        trees.put("A 1", null);
        trees.put("B 1", null);

        assertThat(RequirementSelector.select(List.of(g), known(trees), Set.of("A 1")))
                .containsExactly("B 1");
    }

    @Test
    void chooseNPicksOnlyWhatIsStillNeeded() {
        var g = group("CHOOSE_N", 2, List.of("A 1", "B 1", "C 1"), List.of());
        Map<String, PrereqNode> trees = new LinkedHashMap<>();
        trees.put("A 1", null);
        trees.put("B 1", null);
        trees.put("C 1", null);

        assertThat(RequirementSelector.select(List.of(g), known(trees), Set.of("A 1"))).hasSize(1);
    }

    @Test
    void childPickCountsTowardParent() {
        var child = group("CHOOSE_N", 1, List.of("A 1"), List.of());
        var parent = group("CHOOSE_N", 2, List.of("A 1", "B 1", "C 1"), List.of(child));
        Map<String, PrereqNode> trees = new LinkedHashMap<>();
        trees.put("A 1", null);
        trees.put("B 1", null);
        trees.put("C 1", null);

        assertThat(RequirementSelector.select(List.of(parent), known(trees), Set.of()))
                .containsExactly("A 1", "B 1");
    }

    @Test
    void prefersCoursesWhosePrerequisitesAreAlreadyCovered() {
        var g = group("CHOOSE_N", 1, List.of("B 1", "C 1"), List.of());
        Map<String, PrereqNode> trees = new LinkedHashMap<>();
        trees.put("X 1", null);
        trees.put("B 1", course("X 1"));
        trees.put("C 1", null);

        assertThat(RequirementSelector.select(List.of(g), known(trees), Set.of()))
                .containsExactly("C 1");
    }
}