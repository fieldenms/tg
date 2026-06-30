package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import static ua.com.fielden.platform.web.layout.grid.impl.GridStyles.escape;

/// A single column or row track of a [GridLayoutConfiguration].
///
/// A track carries its CSS size (e.g. `1fr`, `minmax(20%, 1fr)`, `auto`), an optional repeat count, and optional CSS declarations that the layout emulates per cell.
/// The client builds `grid-template-columns` / `grid-template-rows` from the size and repeat count, and applies the declarations to every cell that lands in the track (CSS Grid has no per-track alignment or styling).
/// Self-alignment supplied via a column's `justify` or a row's `align` is stored here as a `justify-self` / `align-self` declaration.
///
class GridTrack {

    private final String size;
    private final Map<String, String> styles = new LinkedHashMap<>();
    private int times = 1;
    private String autoRepeat;

    GridTrack(final String size) {
        this.size = size;
    }

    void putStyle(final String property, final String value) {
        styles.put(property, value);
    }

    void repeat(final int times) {
        this.times = times;
    }

    /// Marks this track to be auto-tracked as `repeat(auto-fit, size)` or `repeat(auto-fill, size)`.
    /// `keyword` is `auto-fit` or `auto-fill`; the number of generated tracks is determined by the browser.
    ///
    void autoRepeat(final String keyword) {
        this.autoRepeat = keyword;
    }

    /// The number of logical tracks this definition represents: its fixed `repeat` count, or 1 for a single or auto-tracked track (the browser determines an auto-track's count).
    ///
    int span() {
        return autoRepeat == null && times > 1 ? times : 1;
    }

    /// Renders this track as a JavaScript object literal, e.g. `{size:"1fr",repeat:3,style:{"padding-left":"32px"}}`,
    /// or `{size:"minmax(220px, 1fr)",repeat:"auto-fit"}` for an auto-tracked column.
    ///
    String render() {
        final StringBuilder sb = new StringBuilder("{size:\"").append(escape(size)).append("\"");
        if (autoRepeat != null) {
            sb.append(",repeat:\"").append(autoRepeat).append("\"");
        } else if (times > 1) {
            sb.append(",repeat:").append(times);
        }
        if (!styles.isEmpty()) {
            sb.append(",style:").append(GridStyles.object(styles));
        }
        return sb.append("}").toString();
    }
}