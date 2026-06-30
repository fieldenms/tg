package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.Map;
import java.util.stream.Collectors;

/// Renders an ordered map of CSS declarations as a JavaScript object literal, e.g. `{"padding-left":"32px","color":"red"}`.
/// Shared by the container, the column and row tracks, and the cells — all of which carry their declarations as `property: value` pairs.
///
final class GridStyles {

    private GridStyles() {
    }

    /// Escapes a developer-supplied value for embedding inside a double-quoted JavaScript string literal in the wire format — backslash, double quote and line breaks.
    /// `StringEscapeUtils.escapeEcmaScript` is deliberately not used: it also escapes `/` and `'`, which are unnecessary inside a double-quoted literal and would needlessly mangle html snippets (`</b>` → `<\/b>`) and CSS values.
    ///
    static String escape(final String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    static String object(final Map<String, String> styles) {
        return "{" + styles.entrySet().stream()
                .map(entry -> "\"" + escape(entry.getKey()) + "\":\"" + escape(entry.getValue()) + "\"")
                .collect(Collectors.joining(",")) + "}";
    }
}