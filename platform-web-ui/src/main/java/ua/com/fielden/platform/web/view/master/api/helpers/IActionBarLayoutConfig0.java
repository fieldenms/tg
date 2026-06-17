package ua.com.fielden.platform.web.view.master.api.helpers;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.FlexLayoutConfiguration;
import ua.com.fielden.platform.web.layout.ILayoutConfiguration;

public interface IActionBarLayoutConfig0<T extends AbstractEntity<?>> {

    /// Sets the master's action bar layout of any kind (flex, grid, …) for the given `device` and `orientation`.
    /// The kind is carried by `layout` (an [ILayoutConfiguration]), so this contract is independent of the layout kind.
    ///
    IActionBarLayoutConfig1<T> setActionBarLayoutFor(final Device device, final Optional<Orientation> orientation, final ILayoutConfiguration layout);

    /// Sets a flex action bar layout for the given `device` and `orientation` from its layout string.
    /// A convenience over [#setActionBarLayoutFor(Device, Optional, ILayoutConfiguration)] that wraps `flexString` into a [FlexLayoutConfiguration].
    ///
    default IActionBarLayoutConfig1<T> setActionBarLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString) {
        return setActionBarLayoutFor(device, orientation, new FlexLayoutConfiguration(flexString));
    }
}
