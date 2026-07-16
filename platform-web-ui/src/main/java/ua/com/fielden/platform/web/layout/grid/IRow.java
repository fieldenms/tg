package ua.com.fielden.platform.web.layout.grid;

/// The step at which a row track has been added and may be further configured.
/// More rows may be added, the current row may be styled, aligned or repeated, or — since the declared tracks already form a complete layout — it may be used as is (all editors auto-flow) or completed with explicit elements.
///
/// This step is itself an [IGridLayoutConfiguration] and may be passed directly to `setLayoutFor`; the trailing `elements(…)` call is optional.
///
public interface IRow extends IGridLayoutConfiguration {

    /// Adds another row track.
    ///
    IRow addRow(String size);

    /// Adds another row track sized `auto` — tall enough to fit its content.
    ///
    IRow addRow();

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

    /// Optionally completes the layout with explicit element configurations (spans, overrides, subheaders, skips).
    /// Ordinary editors that are not named here auto-flow into the remaining cells, in order.
    /// When omitted, the declared tracks are used as the layout directly and every editor auto-flows.
    ///
    IGridLayoutConfiguration elements(IGridElement... cells);
}
