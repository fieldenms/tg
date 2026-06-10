package ua.com.fielden.platform.web.layout.grid;

/// A grid cell started via `cell(row, col)` that must be configured before it can be placed.
/// Every configuration method yields an [IGridCell] — the type that `elements(...)` accepts — so a bare, unconfigured `cell(...)` does not compile, and a placed cell always carries a span, alignment, style or binding.
///
public interface ICell {

    /// Spans this cell across all columns — emitted as `colSpan:"all"` (rendered by the client as `grid-column: 1 / -1`).
    ///
    IGridCell spanAllCols();

    /// Spans this cell across the given number of columns.
    ///
    IGridCell spanCols(int columns);

    /// Spans this cell across the given number of rows.
    ///
    IGridCell spanRows(int rows);

    /// Spans this cell across the given number of columns and rows.
    ///
    IGridCell span(int columns, int rows);

    /// Overrides the cell's alignment along the inline (column) axis — maps to `justify-self`.
    ///
    IGridCell justify(String value);

    /// Overrides the cell's alignment along the block (row) axis — maps to `align-self`.
    ///
    IGridCell align(String value);

    /// Overrides an arbitrary CSS declaration on this cell, taking precedence over any column or row emulated style.
    /// May be called more than once.
    ///
    IGridCell style(String property, String value);

    /// Binds a specific editor to this cell by matching an attribute and value, instead of relying on auto-flow order.
    ///
    IGridCell select(String attribute, String value);
}
