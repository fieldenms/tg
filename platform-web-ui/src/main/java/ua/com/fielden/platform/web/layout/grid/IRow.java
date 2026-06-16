package ua.com.fielden.platform.web.layout.grid;

import ua.com.fielden.platform.web.layout.grid.impl.GridLayoutConfiguration;

/// The step at which a row track has been added and may be further configured.
/// More rows may be added, the current row may be styled, aligned or repeated, or the layout may be completed with its elements.
///
public interface IRow {

    /// Adds another row track.
    ///
    IRow addRow(String size);

    /// Repeats the current row track the specified number of times — emitted as `repeat(n, size)` in the template.
    ///
    IRow repeat(int times);

    /// Emulates a CSS declaration on every cell that lands in the current row.
    /// May be called more than once.
    ///
    IRow style(String property, String value);

    /// Emulates `align-self` on every cell that lands in the current row.
    /// CSS Grid has no native per-row alignment, so this is applied per cell and may be overridden by an element's own alignment.
    ///
    IRow align(String value);

    /// Completes the layout with the given element configurations (spans, overrides, subheaders, skips).
    /// Ordinary editors that are not named here auto-flow into the remaining cells, in order.
    ///
    GridLayoutConfiguration elements(IGridCell... cells);
}
