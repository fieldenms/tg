package ua.com.fielden.platform.web.view.master.api.helpers;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.grid.IGridLayoutConfiguration;

/// The grid action-bar continuation of a master: further grid action-bar breakpoints, then on to the editors layout.
/// It does not expose the flex `setActionBarLayoutFor`, so an action bar that started grid stays grid.
///
public interface IGridActionBarLayoutConfig1<T extends AbstractEntity<?>> extends ILayoutConfig<T> {

    /// Sets a grid action bar layout for a further `device` and `orientation`.
    ///
    IGridActionBarLayoutConfig1<T> setActionBarLayoutFor(final Device device, final Optional<Orientation> orientation, final IGridLayoutConfiguration grid);
}