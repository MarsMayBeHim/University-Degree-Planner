package com.degreeplanner.backend.planner;

import com.degreeplanner.backend.dto.CourseDto;
import com.degreeplanner.backend.dto.RequirementGroupDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class ProgressCalculator {

    private ProgressCalculator() {}

    public static GroupProgress evaluate(RequirementGroupDto group, Set<String> completedCodes) {
        List<GroupProgress> children = group.children().stream()
                .map(c -> evaluate(c, completedCodes))
                .toList();
        boolean childrenDone = children.stream().allMatch(GroupProgress::done);

        List<String> completed = new ArrayList<>();
        List<String> remaining = new ArrayList<>();
        BigDecimal earned = BigDecimal.ZERO;
        for (CourseDto course : group.courses()) {
            if (completedCodes.contains(course.code())) {
                completed.add(course.code());
                earned = earned.add(course.credits());
            } else {
                remaining.add(course.code());
            }
        }

        Integer coursesRequired = null;
        BigDecimal creditsRequired = null;
        boolean ownRuleMet;

        switch (group.ruleType()) {
            case "ALL" -> {
                coursesRequired = group.courses().size();
                ownRuleMet = remaining.isEmpty();
            }
            case "CHOOSE_N" -> {
                int n = group.minCourses() == null ? 0 : group.minCourses();
                coursesRequired = n;
                ownRuleMet = completed.size() >= n;
            }
            case "MIN_CREDITS" -> {
                BigDecimal min = group.minCredits() == null ? BigDecimal.ZERO : group.minCredits();
                creditsRequired = min;
                ownRuleMet = earned.compareTo(min) >= 0;
            }
            default -> throw new IllegalArgumentException("Unknown rule type: " + group.ruleType());
        }

        return new GroupProgress(group.id(), group.name(), group.ruleType(),
                ownRuleMet && childrenDone, completed.size(), coursesRequired,
                earned, creditsRequired, completed, remaining, children);
    }
}