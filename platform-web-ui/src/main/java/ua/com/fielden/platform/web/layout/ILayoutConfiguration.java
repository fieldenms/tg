package ua.com.fielden.platform.web.layout;

/// A device-sensitive layout configuration of a specific kind (flex, grid, or any future kind), ready to be installed on an entity master or centre.
///
/// It decouples the layout-specifying API (`setLayoutFor`) from the concrete layout kind: that API accepts an [ILayoutConfiguration] and never names a kind, so a new kind can be added without touching the master or centre builders.
/// An implementation pairs the serialised layout for a single breakpoint ([#layout()]) with a factory for the matching device-sensitive manager ([#mkLayoutManager(String)]).
///
/// Implementations: [FlexLayoutConfiguration] for flex layouts, and `GridLayoutConfiguration` (in package `grid.impl`) for grid layouts.
///
public interface ILayoutConfiguration {

    /// The serialised layout for a single breakpoint, as understood by the client component of the corresponding layout kind.
    ///
    String layout();

    /// Creates the device-sensitive manager (such as [FlexLayout] or [GridLayout]) that renders this kind of layout.
    /// The `name` provides uniqueness within the source file the manager is generated into.
    ///
    AbstractLayout<? extends AbstractLayoutSetter<?>> mkLayoutManager(final String name);

}