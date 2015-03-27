package ua.com.fielden.platform.web.centre.api.crit.layout;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout;

/**
 * A contract for specifying layouting of selection criteria UI widgets for different devices and orientations.
 *
 * @author TG Team
 *
 */
public interface ILayoutConfig<T extends AbstractEntity<?>> {
    ILayoutConfigWithResultsetSupport<T> setLayoutFor(final ILayout.Device device, final ILayout.Orientation orientation, final String flexString);
}