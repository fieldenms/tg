package ua.com.fielden.platform.web.centre.api.crit.layout;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.FlexLayoutConfiguration;
import ua.com.fielden.platform.web.layout.ILayoutConfiguration;

/// A contract for specifying layouting of selection criteria UI widgets for different devices and orientations.
///
public interface ILayoutConfig<T extends AbstractEntity<?>> {

    /// Installs a selection-criteria layout of any kind (flex, grid, …) for the given `device` and `orientation`.
    /// The kind is carried by `layout` (an [ILayoutConfiguration]), so this contract is independent of the layout kind.
    ///
    ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final ILayoutConfiguration layout);

    /// Installs a flex selection-criteria layout for the given `device` and `orientation` from its layout string.
    /// This is a convenience over [#setLayoutFor(Device, Optional, ILayoutConfiguration)] that wraps `flexString` into a [FlexLayoutConfiguration].
    ///
    default ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        return setLayoutFor(device, orientation, new FlexLayoutConfiguration(flexString));
    }
}