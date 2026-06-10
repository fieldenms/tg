package ua.com.fielden.platform.web.layout.grid;

/// The entry step of a grid layout, returned by `grid()`.
/// The container-level configuration may optionally be set via [#content(IGridContent)]; afterwards (or directly) the column tracks are declared via [#columns()].
///
public interface IContentStep extends IColumnsStep {

    /// Sets the container-level configuration — gaps, content distribution, item alignment defaults and arbitrary styles.
    ///
    IColumnsStep content(IGridContent content);
}
