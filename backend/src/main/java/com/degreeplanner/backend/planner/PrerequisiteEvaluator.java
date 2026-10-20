package com.degreeplanner.backend.planner;

import java.util.Set;

public final class PrerequisiteEvaluator {

    private PrerequisiteEvaluator() {}

    /** A null tree means "no prerequisite", so it is always satisfied. */
    public static boolean isSatisfied(PrereqNode node, Set<String> completedCodes) {
        if (node == null) {
            return true;
        }
        return switch (node.type()) {
            case COURSE -> completedCodes.contains(node.code());
            case AND -> node.children().stream().allMatch(c -> isSatisfied(c, completedCodes));
            case OR -> node.children().stream().anyMatch(c -> isSatisfied(c, completedCodes));
        };
    }
}