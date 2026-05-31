package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/// A contract to specify a rigid or flexible width for a dynamic column.
///
public interface IDynamicColumnBuilderWidth extends IDynamicColumnBuilderWordWrap {

    /// Specifies the rigid column width.
    /// The width of the column won't change when the size of the grid changes.
    ///
    IDynamicColumnBuilderWordWrap width(final int width);

    /// Specifies the flexible column width that will change when the width of the grid changes.
    ///
    IDynamicColumnBuilderWordWrap minWidth(final int width);
}
