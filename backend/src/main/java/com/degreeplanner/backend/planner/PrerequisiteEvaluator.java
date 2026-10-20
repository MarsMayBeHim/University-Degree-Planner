package com.degreeplanner.backend.planner;

import java.util.Set;
import java.util.LinkedHashSet;

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

        /** Course codes in the tree that are not completed, for unsatisfied parts only. */
    public static Set<String> unmetCodes(PrereqNode node, Set<String> completedCodes) {
        Set<String> result = new LinkedHashSet<>();
        collectUnmet(node, completedCodes, result);
        return result;
    }

    private static void collectUnmet(PrereqNode node, Set<String> completed, Set<String> out) {
        if (node == null || isSatisfied(node, completed)) {
            return;
        }
        switch (node.type()) {
            case COURSE -> out.add(node.code());
            case AND, OR -> node.children().forEach(c -> collectUnmet(c, completed, out));
        }
    }
}