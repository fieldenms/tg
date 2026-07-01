package ua.com.fielden.platform.web.centre.api.crit.layout;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.grid.IGridLayoutConfiguration;

/// A contract for specifying layouting of selection criteria UI widgets for different devices and orientations.
/// The first call fixes the layout kind and returns a kind-locked continuation, so every breakpoint shares one kind.
///
public interface ILayoutConfig<T extends AbstractEntity<?>> {

    /// Installs a flex selection-criteria layout for the given `device` and `orientation` from its layout string.
    /// Returns the flex continuation, so subsequent breakpoints are also flex.
    ///
    ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString);

    /// Installs a grid selection-criteria layout for the given `device` and `orientation`.
    /// Returns the grid continuation, so subsequent breakpoints are also grid.
    ///
    IGridLayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final IGridLayoutConfiguration grid);
}