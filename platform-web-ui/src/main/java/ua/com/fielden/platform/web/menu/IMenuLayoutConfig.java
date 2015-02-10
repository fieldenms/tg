package ua.com.fielden.platform.web.menu;

import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

/**
 * The contract for anything that should be layout.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IMenuLayoutConfig<T> {

    /**
     * Specify the layout for specified device and orientation.
     *
     * @param device
     * @param orientation
     * @param layout
     * @return
     */
    T setLayoutFor(final Device device, final Orientation orientation, final String layout);
}
