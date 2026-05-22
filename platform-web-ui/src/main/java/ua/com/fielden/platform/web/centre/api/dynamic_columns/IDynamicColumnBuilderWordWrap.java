package ua.com.fielden.platform.web.centre.api.dynamic_columns;

/// A contract to configure a dynamic column so that its content wraps text automatically and the row height adjusts dynamically.
///
public interface IDynamicColumnBuilderWordWrap extends IDynamicColumnBuilderAddPropWithDone {

    /// Enables word wrapping in an EGI cell for this dynamic column and automatically adjusts its height.
    ///
    IDynamicColumnBuilderAddPropWithDone withWordWrap();
}
