package ua.com.fielden.platform.web.layout.grid;

/// The step at which a column track has been added and may be further configured.
/// More columns may be added, the current column may be styled or justified, the row tracks may be declared, or — since the declared column tracks already form a complete layout — it may be used as is (all editors auto-flow) or completed with explicit elements.
///
/// Because at least one column track has been declared, this step is itself an [IGridLayoutConfiguration] and may be passed directly to `setLayoutFor`; the trailing `elements(…)` call is optional.
///
public interface IColumn extends IGridLayoutConfiguration {

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
    /// Rows are optional — call [#elements(IGridElement...)] directly to leave them implicit (`grid-auto-rows`).
    ///
    IRows rows();

    /// Optionally completes the layout with explicit element configurations (spans, overrides, subheaders, skips).
    /// Ordinary editors that are not named here auto-flow into the remaining cells, in order.
    /// When omitted, the declared tracks are used as the layout directly and every editor auto-flows.
    ///
    IGridLayoutConfiguration elements(IGridElement... cells);
}