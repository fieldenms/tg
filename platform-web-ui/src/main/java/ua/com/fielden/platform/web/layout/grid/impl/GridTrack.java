package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/// A single column or row track of a [GridLayoutConfig].
///
/// A track carries its CSS size (e.g. `1fr`, `minmax(20%, 1fr)`, `auto`), an optional repeat count, and optional CSS declarations that the layout emulates per cell.
/// The size and repeat count feed the container's `grid-template-columns` / `grid-template-rows`.
/// CSS Grid has no per-track alignment or styling, so a track's declarations are applied by the client to every cell that lands in the track.
/// Self-alignment supplied via a column's `justify` or a row's `align` is stored here as a `justify-self` / `align-self` declaration.
///
class GridTrack {

    private final String size;
    private final Map<String, String> styles = new LinkedHashMap<>();
    private int times = 1;

    GridTrack(final String size) {
        this.size = size;
    }

    void putStyle(final String property, final String value) {
        styles.put(property, value);
    }

    void repeat(final int times) {
        this.times = times;
    }

    int times() {
        return times;
    }

    boolean hasStyles() {
        return !styles.isEmpty();
    }

    /// Renders this track's contribution to a `grid-template-*` declaration.
    /// A repeated track collapses to `repeat(n, size)`; otherwise the bare size is returned.
    ///
    String templateToken() {
        return times > 1 ? "repeat(" + times + ", " + size + ")" : size;
    }

    /// Renders this track as a JavaScript object literal carrying its emulated per-cell declarations.
    /// An unstyled track renders as the empty object `{}`.
    ///
    String renderStyleObject() {
        if (styles.isEmpty()) {
            return "{}";
        }
        return "{style:[" + styles.entrySet().stream()
                .map(entry -> "\"" + entry.getKey() + ": " + entry.getValue() + "\"")
                .collect(Collectors.joining(",")) + "]}";
    }
}
