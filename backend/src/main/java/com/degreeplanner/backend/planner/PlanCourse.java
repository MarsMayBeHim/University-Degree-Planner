package com.degreeplanner.backend.planner;

import java.util.Set;

/** A course the generator can schedule. An empty terms set means "any term". */
public record PlanCourse(Long courseId, String code, int credits, PrereqNode tree, Set<String> terms) {}