package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/// An assembled grid layout, produced by completing the fluent chain with `elements(...)`.
///
/// Its [#toString()] renders the wire format consumed by the `tg-grid-layout` client — a single JavaScript object literal of the shape:
/// `{container:[...], columns:[...], rows:[...], cells:[...]}`.
///
/// - `container` — CSS declarations applied to the grid host (`display: grid`, the track templates, gaps, content alignment, arbitrary styles).
/// - `columns` / `rows` — per-track declarations the client emulates on every cell of the track; omitted when no track of that axis carries any.
/// - `cells` — the explicitly configured, non-conforming cells (spans, overrides, subheaders, skips); ordinary editors auto-flow into the rest.
///
public class GridLayoutConfig {

    private final GridContent content;
    private final List<GridTrack> columns;
    private final List<GridTrack> rows;
    private final List<GridCell> cells;

    GridLayoutConfig(final GridContent content, final List<GridTrack> columns, final List<GridTrack> rows, final List<GridCell> cells) {
        this.content = content;
        this.columns = new ArrayList<>(columns);
        this.rows = new ArrayList<>(rows);
        this.cells = new ArrayList<>(cells);
    }

    @Override
    public String toString() {
        final List<String> parts = new ArrayList<>();
        parts.add("container:[" + quote(containerDeclarations()) + "]");
        if (columns.stream().anyMatch(GridTrack::hasStyles)) {
            parts.add("columns:[" + expandStyleObjects(columns) + "]");
        }
        if (rows.stream().anyMatch(GridTrack::hasStyles)) {
            parts.add("rows:[" + expandStyleObjects(rows) + "]");
        }
        parts.add("cells:[" + cells.stream().map(GridCell::render).collect(Collectors.joining(",")) + "]");
        return "{" + String.join(",", parts) + "}";
    }

    /// Builds the list of CSS declarations applied to the grid host, in a deterministic order:
    /// `display: grid`, the column template, the row template (if any), then the container-level declarations.
    ///
    private List<String> containerDeclarations() {
        final List<String> declarations = new ArrayList<>();
        declarations.add("display: grid");
        if (!columns.isEmpty()) {
            declarations.add("grid-template-columns: " + template(columns));
        }
        if (!rows.isEmpty()) {
            declarations.add("grid-template-rows: " + template(rows));
        }
        if (content != null) {
            declarations.addAll(content.declarations());
        }
        return declarations;
    }

    private static String template(final List<GridTrack> tracks) {
        return tracks.stream().map(GridTrack::templateToken).collect(Collectors.joining(" "));
    }

    /// Renders one style object per logical track, expanding a repeated track into the corresponding number of (identical) objects so the array indexes align with the cells' columns/rows.
    ///
    private static String expandStyleObjects(final List<GridTrack> tracks) {
        final List<String> objects = new ArrayList<>();
        for (final GridTrack track : tracks) {
            for (int i = 0; i < track.times(); i++) {
                objects.add(track.renderStyleObject());
            }
        }
        return String.join(",", objects);
    }

    private static String quote(final List<String> declarations) {
        return declarations.stream().map(declaration -> "\"" + declaration + "\"").collect(Collectors.joining(","));
    }
}
