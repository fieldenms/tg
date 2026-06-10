package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.Map;
import java.util.stream.Collectors;

/// Renders an ordered map of CSS declarations as a JavaScript object literal, e.g. `{"padding-left":"32px","color":"red"}`.
/// Shared by the container, the column and row tracks, and the cells — all of which carry their declarations as `property: value` pairs.
///
final class GridStyles {

    private GridStyles() {
    }

    static String object(final Map<String, String> styles) {
        return "{" + styles.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"")
                .collect(Collectors.joining(",")) + "}";
    }
}