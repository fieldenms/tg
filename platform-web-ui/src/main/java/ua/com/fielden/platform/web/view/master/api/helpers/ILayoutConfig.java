package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout;

/**
 * A contract for specifying layouting of UI widgets for different devices and orientations.
 *
 * @author TG Team
 *
 */
public interface ILayoutConfig<T extends AbstractEntity<?>> {
    ILayoutConfigWithDone<T> setLayoutFor(final ILayout.Device device, final ILayout.Orientation orientation, final String flexString);
}
