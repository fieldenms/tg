package ua.com.fielden.platform.web.view.master.api.helpers;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/// The flex action-bar continuation of a master: further flex action-bar breakpoints, then on to the editors layout.
/// It does not expose the grid `setActionBarLayoutFor`, so an action bar that started flex stays flex.
///
public interface IActionBarLayoutConfig1<T extends AbstractEntity<?>> extends ILayoutConfig<T> {

    /// Sets a flex action bar layout for a further `device` and `orientation` from its layout string.
    ///
    IActionBarLayoutConfig1<T> setActionBarLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString);
}