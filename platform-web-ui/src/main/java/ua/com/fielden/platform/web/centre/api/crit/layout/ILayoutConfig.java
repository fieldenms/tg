package ua.com.fielden.platform.web.centre.api.crit.layout;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * A contract for specifying layouting of selection criteria UI widgets for different devices and orientations.
 *
 * @author TG Team
 *
 */
public interface ILayoutConfig<T extends AbstractEntity<?>> {
    ILayoutConfigWithResultsetSupport<T> setLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString);
}