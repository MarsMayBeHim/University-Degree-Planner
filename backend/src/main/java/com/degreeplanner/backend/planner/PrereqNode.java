package com.degreeplanner.backend.planner;

import java.util.List;

public record PrereqNode(Type type, String code, List<PrereqNode> children) {

    public enum Type { COURSE, AND, OR }

    public static PrereqNode course(String code) {
        return new PrereqNode(Type.COURSE, code, List.of());
    }

    public static PrereqNode and(PrereqNode... children) {
        return new PrereqNode(Type.AND, null, List.of(children));
    }

    public static PrereqNode or(PrereqNode... children) {
        return new PrereqNode(Type.OR, null, List.of(children));
    }
}