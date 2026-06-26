package ua.com.fielden.platform.web.layout.grid.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.web.layout.grid.AutoRepeat;
import ua.com.fielden.platform.web.layout.grid.ICell;
import ua.com.fielden.platform.web.layout.grid.IColumn;
import ua.com.fielden.platform.web.layout.grid.IColumns;
import ua.com.fielden.platform.web.layout.grid.IContentStep;
import ua.com.fielden.platform.web.layout.grid.IGridCell;
import ua.com.fielden.platform.web.layout.grid.IGridContent;
import ua.com.fielden.platform.web.layout.grid.IRow;
import ua.com.fielden.platform.web.layout.grid.IRows;

/// The single implementation behind the staged grid-layout fluent API and the home of its static factories.
///
/// One instance implements every step interface of the chain ([IContentStep] through [IRow]).
/// Each chain method returns `this`; because the step interfaces declare narrower return types, the IDE and compiler only offer the methods valid at each step, enforcing the call order
/// `grid().content(…).columns().addColumn(…)….rows().addRow(…)….elements(…)`.
/// Once at least one column has been added the chain is itself an `IGridLayoutConfiguration`, so the trailing `elements(…)` is optional — omit it for a grid whose editors simply auto-flow.
///
/// Intended to be used via a single static import, which brings the whole vocabulary (`grid`, `content`, `cell`, `skip`, `subheader`, `subheaderOpen`, `subheaderClosed`, `html`) into scope.
///
public class GridLayoutBuilder implements IContentStep, IColumns, IColumn, IRows, IRow {

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
    public static IGridCell subheader(final int row, final String title) {
        return GridCell.subheader(row, title);
    }

    /// A collapsible subheader, open by default, at the given row, in column 1, spanning all columns.
    ///
    public static IGridCell subheaderOpen(final int row, final String title) {
        return GridCell.subheaderOpen(row, title);
    }

    /// A collapsible subheader, closed by default, at the given row, in column 1, spanning all columns.
    ///
    public static IGridCell subheaderClosed(final int row, final String title) {
        return GridCell.subheaderClosed(row, title);
    }

    /// An inline html snippet at `(row, col)`, stamped with the layout's `context` (e.g. a label, image or explanatory text).
    ///
    public static IGridCell html(final int row, final int col, final String html) {
        return GridCell.html(row, col, html);
    }

    /// An inline html snippet at `(row, col)`, built from a [DomElement].
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
    public GridLayoutBuilder align(final String value) {
        current.putStyle("align-self", value);
        return this;
    }

    @Override
    public GridLayoutBuilder repeat(final int times) {
        current.repeat(times);
        return this;
    }

    @Override
    public GridLayoutBuilder repeat(final AutoRepeat mode) {
        current.autoRepeat(mode.keyword());
        return this;
    }

    @Override
    public GridLayoutBuilder style(final String property, final String value) {
        current.putStyle(property, value);
        return this;
    }

    @Override
    public GridLayoutConfiguration elements(final IGridCell... cells) {
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
        return new GridLayoutConfiguration(content, columnTracks, rowTracks, cells);
    }
}
