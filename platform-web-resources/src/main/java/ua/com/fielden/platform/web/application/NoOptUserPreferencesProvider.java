package ua.com.fielden.platform.web.application;

import ua.com.fielden.platform.security.user.User;

import java.util.Map;

/// A no-operation implementation of [IUserPreferencesProvider] that returns an empty map.
///
/// This is the default binding — applications that do not need to provide additional user-specific
/// preferences do not need to override this binding.
///
public class NoOptUserPreferencesProvider implements IUserPreferencesProvider {

    @Override
    public Map<String, Object> getPreferencesFor(final User user) {
        return Map.of();
    }

}