package ua.com.fielden.platform.web.view.master.api.helpers;

import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.interfaces.ILayout.Orientation;

public interface IActionBarLayoutConfig0<T extends AbstractEntity<?>> {

    /**
     * Set the layout configuration for master's action bar for specified device and orientation.
     *
     * @param device
     * @param orientation
     * @param flexString
     * @return
     */
    IActionBarLayoutConfig1<T> setActionBarLayoutFor(final Device device, final Optional<Orientation> orientation, final String flexString);
}
