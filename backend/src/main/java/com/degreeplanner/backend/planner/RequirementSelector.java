package com.degreeplanner.backend.planner;

import com.degreeplanner.backend.dto.CourseDto;
import com.degreeplanner.backend.dto.RequirementGroupDto;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RequirementSelector {

    private RequirementSelector() {}

    /** Returns the codes of courses still to be scheduled. */
    public static Set<String> select(List<RequirementGroupDto> roots,
                                     Map<String, PlanCourse> known,
                                     Set<String> completed) {
        Set<String> chosen = new LinkedHashSet<>();
        // Mandatory courses first, so elective choices can see what is already covered
        roots.forEach(g -> addMandatory(g, completed, chosen));
        roots.forEach(g -> selectGroup(g, known, completed, chosen));
        return chosen;
    }

    private static void addMandatory(RequirementGroupDto g, Set<String> completed, Set<String> chosen) {
        if ("ALL".equals(g.ruleType())) {
            for (CourseDto c : g.courses()) {
                if (!completed.contains(c.code())) {
                    chosen.add(c.code());
                }
            }
        }
        g.children().forEach(child -> addMandatory(child, completed, chosen));
    }

    private static void selectGroup(RequirementGroupDto g, Map<String, PlanCourse> known,
                                    Set<String> completed, Set<String> chosen) {
        // Children first: a course picked for a child also counts toward its parent
        g.children().forEach(child -> selectGroup(child, known, completed, chosen));

        List<String> options = g.courses().stream().map(CourseDto::code).toList();
        switch (g.ruleType()) {
            case "CHOOSE_N" -> {
                int n = g.minCourses() == null ? 0 : g.minCourses();
                while (count(options, completed, chosen) < n) {
                    if (!pickBest(options, known, completed, chosen)) {
                        break;
                    }
                }
            }
            case "MIN_CREDITS" -> {
                int min = g.minCredits() == null ? 0 : g.minCredits().intValue();
                while (credits(options, known, completed, chosen) < min) {
                    if (!pickBest(options, known, completed, chosen)) {
                        break;
                    }
                }
            }
            default -> { } // ALL groups are handled by addMandatory
        }
    }

    private static int count(List<String> options, Set<String> completed, Set<String> chosen) {
        return (int) options.stream()
                .filter(c -> completed.contains(c) || chosen.contains(c))
                .count();
    }

    private static int credits(List<String> options, Map<String, PlanCourse> known,
                               Set<String> completed, Set<String> chosen) {
        return options.stream()
                .filter(c -> completed.contains(c) || chosen.contains(c))
                .mapToInt(c -> known.containsKey(c) ? known.get(c).credits() : 0)
                .sum();
    }

    /** Adds the easiest-to-schedule remaining option. Returns false if none are left. */
    private static boolean pickBest(List<String> options, Map<String, PlanCourse> known,
                                    Set<String> completed, Set<String> chosen) {
        Set<String> have = new HashSet<>(completed);
        have.addAll(chosen);
        Set<String> universe = new HashSet<>(known.keySet());
        universe.addAll(completed);

        Optional<String> best = options.stream()
                .filter(c -> !have.contains(c) && known.containsKey(c))
                .min(Comparator.comparingInt((String c) -> score(known.get(c), have, universe))
                        .thenComparing(Comparator.naturalOrder()));
        best.ifPresent(chosen::add);
        return best.isPresent();
    }

    /** Lower is better. Courses that can never be satisfied get a large penalty. */
    private static int score(PlanCourse c, Set<String> have, Set<String> universe) {
        int unmet = PrerequisiteEvaluator.unmetCodes(c.tree(), have).size();
        boolean reachable = PrerequisiteEvaluator.isSatisfied(c.tree(), universe);
        return unmet + (reachable ? 0 : 1000);
    }
}