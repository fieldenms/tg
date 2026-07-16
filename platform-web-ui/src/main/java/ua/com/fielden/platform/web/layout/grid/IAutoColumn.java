package ua.com.fielden.platform.web.layout.grid;

/// The step after the grid's single auto-tracking column has been declared via [IColumns#addAutoColumn(String, AutoRepeat)].
/// Auto-tracking creates a browser-determined number of equally-sized tracks that the editors auto-flow across, so it must stand alone — no further columns, rows or explicit cells may be added.
/// The column may still be given a per-cell style or alignment, and this step is itself an [IGridLayoutConfiguration], usable directly in `setLayoutFor`.
///
public interface IAutoColumn extends IGridLayoutConfiguration {

    /// Emulates a CSS declaration on every editor that flows through the auto-tracking column.
    /// May be called more than once.
    ///
    IAutoColumn style(String property, String value);

    /// Emulates `justify-self` on every editor that flows through the auto-tracking column.
    ///
    IAutoColumn justify(String value);
}