package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/// A contract to specify either a rigid or a flexible natural width for a dynamic column.
///
/// Both setters write the same underlying *natural width*; they differ only in the resulting flexibility:
/// - [#width(int)] → rigid; the column stays at `width` pixels regardless of EGI size.
/// - [#minWidth(int)] → flexible; the column starts at `width` pixels and may grow to absorb leftover space.
///
/// In neither case does this setter affect the drag-resize floor.
/// The user can manually resize to roughly `MIN_COLUMN_WIDTH` (16 px).
/// The naming `minWidth` reflects the user's intent ("at least this wide, then flex") rather than a CSS constraint.
///
/// Mirrors `ResultSetBuilder.width(int)` / `ResultSetBuilder.minWidth(int)` for static columns.
///
public interface IDynamicColumnBuilderWidth extends IDynamicColumnBuilderWordWrap {

    /// Specifies a **rigid** natural width.
    /// The column won't grow beyond `width` when the EGI is wider than the sum of column widths.
    ///
    IDynamicColumnBuilderWordWrap width(final int width);

    /// Specifies a **flexible** natural width.
    /// The column starts at `width` and may grow to absorb leftover horizontal space.
    /// The argument is the natural width — not the resize floor.
    ///
    IDynamicColumnBuilderWordWrap minWidth(final int width);
}
