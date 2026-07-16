package ua.com.fielden.platform.web.layout.grid;

/// A configuration for a grid layout — the type accepted by `setLayoutFor` for the grid kind.
///
/// It is produced by the grid fluent API: either the assembled `GridLayoutConfiguration` returned by `elements(…)`, or the chain itself once at least one column has been declared (its `IColumn` / `IRow` steps), so a grid may be installed with or without a trailing `elements(…)` call.
/// Its single method yields the serialised layout for one breakpoint, as understood by the `tg-grid-layout` client.
///
public interface IGridLayoutConfiguration {

    /// The serialised grid layout for a single breakpoint.
    ///
    String layout();
}
