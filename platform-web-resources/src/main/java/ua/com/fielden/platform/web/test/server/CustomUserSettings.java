package ua.com.fielden.platform.web.test.server;

import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.CollectionUtil;
import ua.com.fielden.platform.web.application.IUserPreferencesProvider;

import java.util.Map;

public class CustomUserSettings implements IUserPreferencesProvider {

    @Override
    public Map<String, Object> getUserPreferences(final User user) {
        return CollectionUtil.mapOf(
                T2.t2("title", "Custom TG test app"),
                T2.t2("watermark", "Custom config example app")
        );
    }
}
