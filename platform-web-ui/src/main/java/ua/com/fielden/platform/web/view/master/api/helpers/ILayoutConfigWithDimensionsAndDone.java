package ua.com.fielden.platform.web.view.master.api.helpers;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/// The flex-layout continuation of a master: further flex breakpoints, plus dimensions and completion.
/// It does not expose the grid `setLayoutFor`, so a master that started flex stays flex.
///
public interface ILayoutConfigWithDimensionsAndDone<T extends AbstractEntity<?>> extends IDimensions<T>, IComplete<T> {

    /// Installs a flex layout for a further `device` and `orientation` from its layout string.
    ///
    ILayoutConfigWithDimensionsAndDone<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString);
}