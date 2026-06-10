package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/// A single column or row track of a [GridLayoutConfig].
///
/// A track carries its CSS size (e.g. `1fr`, `minmax(20%, 1fr)`, `auto`), an optional repeat count, and optional CSS declarations that the layout emulates per cell.
/// The client builds `grid-template-columns` / `grid-template-rows` from the size and repeat count, and applies the declarations to every cell that lands in the track (CSS Grid has no per-track alignment or styling).
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

    /// Renders this track as a JavaScript object literal, e.g. `{size:"1fr",repeat:3,style:{"padding-left":"32px"}}`.
    ///
    String render() {
        final StringBuilder sb = new StringBuilder("{size:\"").append(size).append("\"");
        if (times > 1) {
            sb.append(",repeat:").append(times);
        }
        if (!styles.isEmpty()) {
            sb.append(",style:").append(GridStyles.object(styles));
        }
        return sb.append("}").toString();
    }
}