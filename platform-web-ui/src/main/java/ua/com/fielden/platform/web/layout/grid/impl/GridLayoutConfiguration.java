package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ua.com.fielden.platform.web.layout.AbstractLayout;
import ua.com.fielden.platform.web.layout.AbstractLayoutSetter;
import ua.com.fielden.platform.web.layout.GridLayout;
import ua.com.fielden.platform.web.layout.ILayoutConfiguration;

/// An assembled grid layout, produced by completing the fluent chain with `elements(...)`.
///
/// Its [#toString()] (and equivalently [#layout()]) renders the wire format consumed by the `tg-grid-layout` client — a single JavaScript object literal of the shape:
/// `{container:{…}, columns:[…], rows:[…], cells:[…]}`.
///
/// - `container` — the container-level CSS declarations as `property: value` pairs (gaps, content distribution, item-alignment defaults, arbitrary styles); omitted when empty. The client supplies `display: grid` itself.
/// - `columns` / `rows` — the track definitions: each carries its `size` (and an optional `repeat`), from which the client builds `grid-template-columns` / `grid-template-rows`, plus an optional `style` it emulates on every cell of the track. `rows` is omitted when left implicit.
/// - `cells` — the explicitly configured, non-conforming cells (spans, overrides, subheaders, skips); ordinary editors auto-flow into the rest.
///
/// As an [ILayoutConfiguration], it can be passed directly to `setLayoutFor` on a master or centre, where it pairs the wire string with a [GridLayout] manager.
///
public class GridLayoutConfiguration implements ILayoutConfiguration {

    private final GridContent content;
    private final List<GridTrack> columns;
    private final List<GridTrack> rows;
    private final List<GridCell> cells;

    GridLayoutConfiguration(final GridContent content, final List<GridTrack> columns, final List<GridTrack> rows, final List<GridCell> cells) {
        this.content = content;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>(rows);
        this.cells = new ArrayList<>(cells);
    }

    @Override
    public String toString() {
        final List<String> parts = new ArrayList<>();
        if (content != null && !content.isEmpty()) {
            parts.add("container:" + content.renderObject());
        }
        if (!columns.isEmpty()) {
            parts.add("columns:[" + renderTracks(columns) + "]");
        }
        if (!rows.isEmpty()) {
            parts.add("rows:[" + renderTracks(rows) + "]");
        }
        parts.add("cells:[" + cells.stream().map(GridCell::render).collect(Collectors.joining(",")) + "]");
        return "{" + String.join(",", parts) + "}";
    }

    @Override
    public String layout() {
        return toString();
    }

    @Override
    public AbstractLayout<? extends AbstractLayoutSetter<?>> mkLayoutManager(final String name) {
        return new GridLayout(name);
    }

    private static String renderTracks(final List<GridTrack> tracks) {
        return tracks.stream().map(GridTrack::render).collect(Collectors.joining(","));
    }
}