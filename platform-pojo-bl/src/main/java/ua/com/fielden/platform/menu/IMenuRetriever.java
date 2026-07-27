package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.web.interfaces.DeviceProfile;

public interface IMenuRetriever {

    /// Returns a [Menu] entity for concrete [DeviceProfile].
    ///
    Menu getMenuEntity(final DeviceProfile deviceProfile);

}