package com.degreeplanner.backend.planner;

import static com.degreeplanner.backend.planner.PrereqNode.and;
import static com.degreeplanner.backend.planner.PrereqNode.course;
import static com.degreeplanner.backend.planner.PrereqNode.or;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PrerequisiteEvaluatorTest {

    @Test
    void noPrerequisiteIsAlwaysSatisfied() {
        assertThat(PrerequisiteEvaluator.isSatisfied(null, Set.of())).isTrue();
    }

    @Test
    void singleCourseNeedsThatCourse() {
        PrereqNode tree = course("CS 110");
        assertThat(PrerequisiteEvaluator.isSatisfied(tree, Set.of("CS 110"))).isTrue();
        assertThat(PrerequisiteEvaluator.isSatisfied(tree, Set.of("CS 112"))).isFalse();
    }

    @Test
    void andNeedsEveryChild() {
        PrereqNode tree = and(course("CS 211"), course("MATH 125"));
        assertThat(PrerequisiteEvaluator.isSatisfied(tree, Set.of("CS 211", "MATH 125"))).isTrue();
        assertThat(PrerequisiteEvaluator.isSatisfied(tree, Set.of("CS 211"))).isFalse();
    }

    @Test
    void orNeedsAtLeastOneChild() {
        PrereqNode tree = or(course("MATH 113"), course("MATH 124"));
        assertThat(PrerequisiteEvaluator.isSatisfied(tree, Set.of("MATH 124"))).isTrue();
        assertThat(PrerequisiteEvaluator.isSatisfied(tree, Set.of())).isFalse();
    }

    @Test
    void nestedTreeLikeCs310() {
        PrereqNode tree = and(
                course("CS 211"),
                course("MATH 125"),
                or(course("MATH 113"), course("MATH 124"), course("MATH 115")));

        assertThat(PrerequisiteEvaluator.isSatisfied(tree,
                Set.of("CS 211", "MATH 125", "MATH 113"))).isTrue();
        assertThat(PrerequisiteEvaluator.isSatisfied(tree,
                Set.of("CS 211", "MATH 125"))).isFalse();
    }
}