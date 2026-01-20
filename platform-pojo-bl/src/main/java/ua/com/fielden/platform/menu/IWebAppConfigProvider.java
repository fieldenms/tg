package ua.com.fielden.platform.menu;

import ua.com.fielden.platform.web.interfaces.DeviceProfile;

/// A contract to accumulate client web application configuration (menu, time/date formats etc.)
///
public interface IWebAppConfigProvider {
    
    /// Returns a [Menu] entity for concrete [DeviceProfile].
    ///
    Menu getMenuEntity(final DeviceProfile deviceProfile);

    /// Returns true if server and client applications operate in the same time-zone, otherwise false.
    /// The only exception is handling of 'now': it calculates based on real user time-zone (and later converts to server time-zone).
    ///
    boolean independentTimeZone();
}
