package com.degreeplanner.backend.planner;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PrereqTreeParserTest {

    private static final String CS_310 = """
            {"type":"AND","children":[
              {"type":"COURSE","code":"CS 211"},
              {"type":"COURSE","code":"MATH 125"},
              {"type":"OR","children":[
                {"type":"COURSE","code":"MATH 113"},
                {"type":"COURSE","code":"MATH 124"}]}]}
            """;

    @Test
    void nullOrBlankMeansNoPrerequisite() {
        assertThat(PrereqTreeParser.parse(null)).isNull();
        assertThat(PrereqTreeParser.parse("  ")).isNull();
    }

    @Test
    void parsedTreeEvaluatesCorrectly() {
        PrereqNode tree = PrereqTreeParser.parse(CS_310);
        assertThat(PrerequisiteEvaluator.isSatisfied(tree,
                Set.of("CS 211", "MATH 125", "MATH 124"))).isTrue();
        assertThat(PrerequisiteEvaluator.isSatisfied(tree,
                Set.of("CS 211", "MATH 125"))).isFalse();
    }

    @Test
    void unmetCodesListsOnlyWhatIsMissing() {
        PrereqNode tree = PrereqTreeParser.parse(CS_310);
        assertThat(PrerequisiteEvaluator.unmetCodes(tree, Set.of("CS 211")))
                .containsExactly("MATH 125", "MATH 113", "MATH 124");
    }
}