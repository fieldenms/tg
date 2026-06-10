package ua.com.fielden.platform.web.layout.grid;

/// A fully-configured grid cell, ready to be placed via `elements(...)`.
/// It is produced by configuring an [ICell] obtained from `cell(row, col)`, or directly by the complete `skip(...)`, `subheader(...)`, `subheaderOpen(...)` and `subheaderClosed(...)` factories.
/// Being an [ICell] as well, a configured cell may be configured further (e.g. a subheader given a `style`).
///
public interface IGridCell extends ICell {
}
