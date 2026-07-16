package ua.com.fielden.platform.web.layout.grid;

/// A fully-configured grid cell, ready to be placed via `elements(...)`.
/// It is produced by configuring an [ICell] obtained from `cell(row, col)`, or directly by the `skip(...)` and `html(...)` factories.
/// Being an [ICell] as well, a configured cell may be configured further (e.g. given a span and then a `style`).
/// Subheaders are [ISubheader]s rather than [IGridCell]s, since their column span is fixed.
///
public interface IGridCell extends ICell, IGridElement {
}
