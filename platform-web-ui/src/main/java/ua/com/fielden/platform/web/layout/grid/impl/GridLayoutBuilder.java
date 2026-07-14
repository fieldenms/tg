package ua.com.fielden.platform.web.layout.grid.impl;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.grid.*;
import ua.com.fielden.platform.web.layout.grid.exceptions.GridLayoutConfigurationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/// The single implementation behind the staged grid-layout fluent API and the home of its static factories.
///
/// One instance implements every step interface of the chain ([IContentStep] through [IRow]).
/// Each chain method returns `this`; because the step interfaces declare narrower return types, the IDE and compiler only offer the methods valid at each step, enforcing the call order
/// `grid().content(…).columns().addColumn(…)….rows().addRow(…)….elements(…)`.
/// Once at least one column has been added the chain is itself an `IGridLayoutConfiguration`, so the trailing `elements(…)` is optional — omit it for a grid whose editors simply auto-flow.
///
/// Intended to be used via a single static import, which brings the whole vocabulary (`grid`, `content`, `cell`, `skip`, `subheader`, `subheaderOpen`, `subheaderClosed`, `html`) into scope.
///
public class GridLayoutBuilder implements IContentStep, IColumns, IColumn, IAutoColumn, IRows, IRow {

    public static final String
        ERR_COLUMN_OUT_OF_BOUNDS = "Grid cell at row %s, column %s is outside the layout's %s declared column(s).",
        ERR_COLUMN_SPAN_OUT_OF_BOUNDS = "Grid cell at row %s, column %s spans to column %s, past the layout's %s declared column(s).",
        ERR_NON_POSITIVE_ROW = "Grid cell at column %s has a non-positive row %s.",
        ERR_ROW_OUT_OF_BOUNDS = "Grid cell at row %s, column %s is outside the layout's %s declared row(s).",
        ERR_ROW_SPAN_OUT_OF_BOUNDS = "Grid cell at row %s, column %s spans to row %s, past the layout's %s declared row(s).",
        ERR_OVERLAPPING_CELLS = "Grid cell at row %s, column %s overlaps the cell at row %s, column %s.",
        ERR_INVALID_REPEAT = "Track repeat count must be at least 1, but was %s.";

    private GridContent content;
    private final List<GridTrack> columnTracks = new ArrayList<>();
    private final List<GridTrack> rowTracks = new ArrayList<>();
    private GridTrack current;

    private GridLayoutBuilder() {
    }

    // ------------------------------------------------------------------------
    // Static factories — a single static import brings the whole vocabulary in.
    // ------------------------------------------------------------------------

    /// Begins a grid layout.
    ///
    public static IContentStep grid() {
        return new GridLayoutBuilder();
    }

    /// Creates a container-level configuration to be passed to [IContentStep#content(IGridContent)].
    ///
    public static IGridContent content() {
        return new GridContent();
    }

    /// Starts an ordinary cell at `(row, col)`; it must be configured (a span, alignment, style or `select`) before it can be placed.
    ///
    public static ICell cell(final int row, final int col) {
        return GridCell.cell(row, col);
    }

    /// An empty (skipped) cell at `(row, col)`.
    ///
    public static IGridCell skip(final int row, final int col) {
        return GridCell.skip(row, col);
    }

    /// A non-collapsible subheader at the given row, in column 1, spanning all columns.
    ///
    public static ISubheader subheader(final int row, final String title) {
        return GridCell.subheader(row, title);
    }

    /// A collapsible subheader, open by default, at the given row, in column 1, spanning all columns.
    ///
    public static ISubheader subheaderOpen(final int row, final String title) {
        return GridCell.subheaderOpen(row, title);
    }

    /// A collapsible subheader, closed by default, at the given row, in column 1, spanning all columns.
    ///
    public static ISubheader subheaderClosed(final int row, final String title) {
        return GridCell.subheaderClosed(row, title);
    }

    /// An inline html snippet at `(row, col)`, stamped with the layout's `context` (e.g. a label, image or explanatory text).
    ///
    /// The markup must be developer-authored and trusted: it is rendered into the DOM as-is (stamped via `innerHTML` on the client), so it must never incorporate user- or entity-supplied data.
    ///
    public static IGridCell html(final int row, final int col, final String html) {
        return GridCell.html(row, col, html);
    }

    /// An inline html snippet at `(row, col)`, built from a [DomElement].
    /// As with [#html(int, int, String)], the resulting markup must be developer-authored and trusted.
    ///
    public static IGridCell html(final int row, final int col, final DomElement dom) {
        return GridCell.html(row, col, dom.toString());
    }

    // ------------------------------------------------------------------------
    // Chain — every method returns `this`; the step interfaces narrow the view.
    // ------------------------------------------------------------------------

    @Override
    public GridLayoutBuilder content(final IGridContent content) {
        this.content = (GridContent) content;
        return this;
    }

    @Override
    public GridLayoutBuilder columns() {
        return this;
    }

    @Override
    public GridLayoutBuilder addColumn(final String size) {
        current = new GridTrack(size);
        columnTracks.add(current);
        return this;
    }

    @Override
    public GridLayoutBuilder addColumn() {
        return addColumn("1fr");
    }

    @Override
    public GridLayoutBuilder addAutoColumn(final String size, final AutoRepeat mode) {
        current = new GridTrack(size);
        current.autoRepeat(mode.keyword());
        columnTracks.add(current);
        return this;
    }

    @Override
    public GridLayoutBuilder justify(final String value) {
        current.putStyle("justify-self", value);
        return this;
    }

    @Override
    public GridLayoutBuilder rows() {
        return this;
    }

    @Override
    public GridLayoutBuilder addRow(final String size) {
        current = new GridTrack(size);
        rowTracks.add(current);
        return this;
    }

    @Override
    public GridLayoutBuilder addRow() {
        return addRow("auto");
    }

    @Override
    public GridLayoutBuilder align(final String value) {
        current.putStyle("align-self", value);
        return this;
    }

    @Override
    public GridLayoutBuilder repeat(final int times) {
        if (times < 1) {
            throw new GridLayoutConfigurationException(ERR_INVALID_REPEAT.formatted(times));
        }
        current.repeat(times);
        return this;
    }

    @Override
    public GridLayoutBuilder style(final String property, final String value) {
        current.putStyle(property, value);
        return this;
    }

    @Override
    public GridLayoutConfiguration elements(final IGridElement... cells) {
        return build(Stream.of(cells).map(GridCell.class::cast).collect(Collectors.toList()));
    }

    @Override
    public String layout() {
        return build(List.of()).layout();
    }

    /// Assembles the configured content, column and row tracks with the given explicit cells into an immutable [GridLayoutConfiguration].
    /// When no `elements(…)` are supplied (the chain used directly as a configuration), the cell list is empty and every editor auto-flows.
    ///
    private GridLayoutConfiguration build(final List<GridCell> cells) {
        validateCells(cells);
        applySubheaderStyles(cells);
        return new GridLayoutConfiguration(content, columnTracks, rowTracks, cells);
    }

    /// Folds the container-level subheader-style defaults (from `content().withSubheaderStyle(...)`) into every subheader cell's own styles.
    /// A per-subheader `style(...)` for the same property wins; a container declaration only fills a property the subheader has not set.
    /// A no-op when no content is configured, when no subheader styles are declared, or when the layout contains no subheaders.
    ///
    private void applySubheaderStyles(final List<GridCell> cells) {
        if (content == null || content.subheaderStyles().isEmpty()) {
            return;
        }
        for (final GridCell cell : cells) {
            if (cell.isSubheader()) {
                cell.addDefaultStyles(content.subheaderStyles());
            }
        }
    }

    /// Rejects misconfigured explicit cells before the layout is assembled:
    /// a cell anchored outside the declared grid — a column outside `1..N` (N = declared columns), or, when explicit rows are declared, a row outside `1..M` (M = declared rows); with implicit rows only a non-positive row;
    /// a cell whose span reaches past the last column (or, with explicit rows, past the last row), which would otherwise make the client expand the grid with implicit tracks;
    /// or a cell whose occupied region (accounting for spans) overlaps a position already taken by an earlier cell.
    /// Each would leave the client unable to place the cell as declared — an unreachable coordinate hangs an implicit-row layout, an over-span silently grows the grid — so they are configuration errors caught here rather than at render time.
    ///
    private void validateCells(final List<GridCell> cells) {
        final int columnCount = columnTracks.stream().mapToInt(GridTrack::span).sum();
        final boolean hasExplicitRows = !rowTracks.isEmpty();
        final int rowCount = hasExplicitRows ? rowTracks.stream().mapToInt(GridTrack::span).sum() : Integer.MAX_VALUE;
        final Map<String, GridCell> occupied = new HashMap<>();
        for (final GridCell cell : cells) {
            if (cell.firstColumn() < 1 || cell.firstColumn() > columnCount) {
                throw new GridLayoutConfigurationException(ERR_COLUMN_OUT_OF_BOUNDS.formatted(cell.row(), cell.col(), columnCount));
            }
            if (cell.row() < 1) {
                throw new GridLayoutConfigurationException(ERR_NON_POSITIVE_ROW.formatted(cell.col(), cell.row()));
            }
            if (hasExplicitRows && cell.row() > rowCount) {
                throw new GridLayoutConfigurationException(ERR_ROW_OUT_OF_BOUNDS.formatted(cell.row(), cell.col(), rowCount));
            }
            final int firstCol = cell.firstColumn();
            final int lastCol = firstCol + cell.occupiedColumns(columnCount) - 1;
            final int lastRow = cell.row() + cell.occupiedRows() - 1;
            if (lastCol > columnCount) {
                throw new GridLayoutConfigurationException(ERR_COLUMN_SPAN_OUT_OF_BOUNDS.formatted(cell.row(), cell.col(), lastCol, columnCount));
            }
            if (hasExplicitRows && lastRow > rowCount) {
                throw new GridLayoutConfigurationException(ERR_ROW_SPAN_OUT_OF_BOUNDS.formatted(cell.row(), cell.col(), lastRow, rowCount));
            }
            for (int r = cell.row(); r <= lastRow; r += 1) {
                for (int c = firstCol; c <= lastCol; c += 1) {
                    final GridCell other = occupied.putIfAbsent(r + "," + c, cell);
                    if (other != null) {
                        throw new GridLayoutConfigurationException(ERR_OVERLAPPING_CELLS.formatted(cell.row(), cell.col(), other.row(), other.col()));
                    }
                }
            }
        }
    }
}
