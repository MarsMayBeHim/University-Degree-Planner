package com.degreeplanner.backend.planner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlanGenerator {

    public record Settings(int startYear, String startTerm, int semesterCount, int minCredits, int maxCredits) {
        // Keeps older callers and tests working: no minimum
        public Settings(int startYear, String startTerm, int semesterCount, int maxCredits) {
            this(startYear, startTerm, semesterCount, 0, maxCredits);
        }
    }

    public record Placement(int semesterIndex, int year, String term, String code) {}

    public record Unplaced(String code, String reason) {}

    public record LightSemester(int year, String term, int credits) {}

    public record Result(List<Placement> placements, List<Unplaced> unplaced, List<LightSemester> lightSemesters) {}

    /** Numeric part of a code like "CS 211", or 0 when there isn't one. */
    private static int level(String code) {
        int space = code.indexOf(' ');
        String digits = space < 0 ? "" : code.substring(space + 1).replaceAll("\\D.*$", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }

    private PlanGenerator() {}

    public static Result generate(Collection<PlanCourse> needed, Set<String> completed, Settings settings) {
        Map<String, PlanCourse> byCode = new LinkedHashMap<>();
        for (PlanCourse c : needed) {
            byCode.putIfAbsent(c.code(), c);
        }

        Map<String, Integer> depth = depths(byCode);
        Set<String> available = new HashSet<>(completed);
        Set<String> placed = new HashSet<>();
        List<Placement> placements = new ArrayList<>();
        List<LightSemester> semesterTotals = new ArrayList<>();

        int year = settings.startYear();
        String term = settings.startTerm();

        for (int i = 0; i < settings.semesterCount(); i++) {
            final String semTerm = term;

            List<PlanCourse> candidates = byCode.values().stream()
                    .filter(c -> !placed.contains(c.code()))
                    .filter(c -> c.terms().isEmpty() || c.terms().contains(semTerm))
                    .filter(c -> PrerequisiteEvaluator.isSatisfied(c.tree(), available))
                    .sorted(Comparator.comparingInt((PlanCourse c) -> -depth.get(c.code()))
                            .thenComparingInt(c -> level(c.code()))
                            .thenComparing(PlanCourse::code))
                    .toList();

            int credits = 0;
            List<String> thisSemester = new ArrayList<>();
            for (PlanCourse c : candidates) {
                if (credits + c.credits() <= settings.maxCredits()) {
                    credits += c.credits();
                    thisSemester.add(c.code());
                    placements.add(new Placement(i, year, semTerm, c.code()));
                }
            }
            semesterTotals.add(new LightSemester(year, semTerm, credits));

            // Courses only satisfy prerequisites for LATER semesters
            available.addAll(thisSemester);
            placed.addAll(thisSemester);

            if ("FALL".equals(term)) {
                term = "SPRING";
                year++;
            } else {
                term = "FALL";
            }
        }

        List<Unplaced> unplaced = new ArrayList<>();
        for (PlanCourse c : byCode.values()) {
            if (placed.contains(c.code())) {
                continue;
            }
            Set<String> unmet = PrerequisiteEvaluator.unmetCodes(c.tree(), available);
            String reason;
            if (c.credits() > settings.maxCredits()) {
                reason = "Has more credits than the semester limit";
            } else if (!unmet.isEmpty()) {
                reason = "Prerequisites not met: " + String.join(", ", unmet);
            } else {
                reason = "No room within the semester limit, or not offered in the available terms";
            }
            unplaced.add(new Unplaced(c.code(), reason));
        }
        int lastUsed = placements.stream().mapToInt(Placement::semesterIndex).max().orElse(-1);
        List<LightSemester> light = new ArrayList<>();
        for (int i = 0; i < lastUsed; i++) {
            if (semesterTotals.get(i).credits() < settings.minCredits()) {
                light.add(semesterTotals.get(i));
            }
        }
        return new Result(placements, unplaced, light);
    }

    /** Length of the longest chain of needed courses that depends on each course. */
    private static Map<String, Integer> depths(Map<String, PlanCourse> byCode) {
        Map<String, Set<String>> dependents = new HashMap<>();
        for (PlanCourse c : byCode.values()) {
            for (String prereq : PrerequisiteEvaluator.referencedCodes(c.tree())) {
                if (byCode.containsKey(prereq)) {
                    dependents.computeIfAbsent(prereq, k -> new HashSet<>()).add(c.code());
                }
            }
        }
        Map<String, Integer> memo = new HashMap<>();
        for (String code : byCode.keySet()) {
            depth(code, dependents, memo, new HashSet<>());
        }
        return memo;
    }

    private static int depth(String code, Map<String, Set<String>> dependents,
                             Map<String, Integer> memo, Set<String> visiting) {
        Integer cached = memo.get(code);
        if (cached != null) {
            return cached;
        }
        if (!visiting.add(code)) {
            throw new IllegalStateException("Prerequisite cycle involving " + code);
        }
        int best = 0;
        for (String dependent : dependents.getOrDefault(code, Set.of())) {
            best = Math.max(best, depth(dependent, dependents, memo, visiting));
        }
        visiting.remove(code);
        memo.put(code, best + 1);
        return best + 1;
    }
}