package ua.com.fielden.platform.web.resources.webui;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.Optional;

/// Convenient record holding Entity Centre configuration settings.
///
/// @param saveAsName optional "save-as" name for named configuration or empty [Optional] for default one
/// @param owner      a [User] that created the configuration (or own it through inheritance process)
/// @param device     indicates whether the configuration belongs to [DeviceProfile#DESKTOP] namespace or [DeviceProfile#MOBILE]
/// @param miType     menu item type for the configuration
///
public record ConfigSettings(
    Optional<String> saveAsName,
    User owner,
    DeviceProfile device,
    Class<? extends MiWithConfigurationSupport<?>> miType
) {}
