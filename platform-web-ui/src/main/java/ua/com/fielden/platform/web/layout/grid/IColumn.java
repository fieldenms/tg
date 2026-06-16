package ua.com.fielden.platform.web.layout.grid;

import ua.com.fielden.platform.web.layout.grid.impl.GridLayoutConfiguration;

/// The step at which a column track has been added and may be further configured.
/// More columns may be added, the current column may be styled or justified, the row tracks may be declared, or the layout may be completed with its elements.
///
public interface IColumn {

    /// Adds another column track.
    ///
    IColumn addColumn(String size);

    /// Adds another column track sized `1fr` — an equal share of the free space.
    ///
    IColumn addColumn();

    /// Repeats the current column track the specified number of times — emitted as `repeat(n, size)` in the template.
    ///
    IColumn repeat(int times);

    /// Emulates a CSS declaration on every cell that lands in the current column.
    /// May be called more than once.
    ///
    IColumn style(String property, String value);

    /// Emulates `justify-self` on every cell that lands in the current column.
    /// CSS Grid has no native per-column alignment, so this is applied per cell and may be overridden by an element's own alignment.
    ///
    IColumn justify(String value);

    /// Begins declaration of the grid's row tracks.
    /// Rows are optional — call [#elements(IGridCell...)] directly to leave them implicit (`grid-auto-rows`).
    ///
    IRows rows();

    /// Completes the layout with the given element configurations (spans, overrides, subheaders, skips).
    /// Ordinary editors that are not named here auto-flow into the remaining cells, in order.
    ///
    GridLayoutConfiguration elements(IGridCell... cells);
}