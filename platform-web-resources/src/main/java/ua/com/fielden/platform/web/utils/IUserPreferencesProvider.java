package ua.com.fielden.platform.web.utils;

import com.google.inject.ImplementedBy;
import ua.com.fielden.platform.security.user.User;

import java.util.Map;

/// A contract for providing user-specific application preferences delivered to the client as part of the application configuration.
///
/// The returned key/value pairs are merged into the application configuration response sent to the frontend,
/// on top of the standard platform-level configuration provided by [ApplicationConfigurationResource].
/// Entries with keys matching the standard configuration keys (e.g., `locale`, `dateFormat`, `timeZone`) will override the default values.
///
/// Implement and bind this interface in an application-specific IoC module to provide user-specific preferences.
///
@FunctionalInterface
@ImplementedBy(NoOptUserPreferencesProvider.class)
public interface IUserPreferencesProvider {

    /// Returns a map of preference keys and their values specific to `user`.
    /// Entries with keys matching the standard platform-level configuration will override the defaults.
    ///
    Map<String, Object> getUserPreferences(final User user);

}
