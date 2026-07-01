package ua.com.fielden.platform.web.view.master.api.helpers;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;
import ua.com.fielden.platform.web.layout.grid.IGridLayoutConfiguration;

/// The grid-layout continuation of a master: further grid breakpoints, plus dimensions and completion.
/// It does not expose the flex `setLayoutFor`, so a master that started grid stays grid.
///
public interface IGridLayoutConfigWithDimensionsAndDone<T extends AbstractEntity<?>> extends IDimensions<T>, IComplete<T> {

    /// Installs a grid layout for a further `device` and `orientation`.
    ///
    IGridLayoutConfigWithDimensionsAndDone<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final IGridLayoutConfiguration grid);
}