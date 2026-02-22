package ua.com.fielden.platform.web.resources.webui;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;
import ua.com.fielden.platform.web.interfaces.DeviceProfile;

import java.util.Optional;

public record ConfigSettings(
        Optional<String> saveAsName,
        User owner,
        DeviceProfile device,
        Class<? extends MiWithConfigurationSupport<?>> miType
) {
}
