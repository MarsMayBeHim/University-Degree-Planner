package com.degreeplanner.backend.planner;

import java.util.List;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

public final class PrereqTreeParser {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PrereqTreeParser() {}

    /** Returns null when there is no prerequisite tree. */
    public static PrereqNode parse(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            Map<?, ?> map = MAPPER.readValue(json, Map.class);
            return toNode(map);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid prerequisite tree: " + json, e);
        }
    }

    private static PrereqNode toNode(Map<?, ?> map) {
        PrereqNode.Type type = PrereqNode.Type.valueOf(String.valueOf(map.get("type")));
        if (type == PrereqNode.Type.COURSE) {
            return PrereqNode.course(String.valueOf(map.get("code")));
        }
        List<?> rawChildren = (List<?>) map.get("children");
        List<PrereqNode> children = rawChildren.stream()
                .map(c -> toNode((Map<?, ?>) c))
                .toList();
        return new PrereqNode(type, null, children);
    }
}