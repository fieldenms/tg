package ua.com.fielden.platform.web.view.master.api.helpers;

import ua.com.fielden.platform.web.interfaces.ILayout;

/**
 * A contract for specifying layouting of UI widgets for different devices and orientations.
 *
 * @author TG Team
 *
 */
public interface ILayoutConfig {
    ILayoutConfigWithDone setLayoutFor(final ILayout.Device device, final ILayout.Orientation orientation, final String flexString);
}
