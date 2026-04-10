package ua.com.fielden.platform.basic.config;

import com.google.inject.ImplementedBy;

/// A contract that represents core settings for TG-based applications.
///
@ImplementedBy(ApplicationSettings.class)
public interface IApplicationSettings {
    String appHome();

    String classPath();

    String packageName();

    String pathToStorage();

    String pathToStorageFor(Class<?> type);

    String pathToSecurityTokens();

    String securityTokensPackageName();

    /// A currency symbol that should be used to display monetary values.
    String currencySymbol();

    Workflows workflow();
    
    AuthMode authMode();

    /// Indicates whether users are permitted to edit their own [User] record.
    /// Defaults to `true` for backward compatibility.
    /// When set to `false`, a user's own [User] record is read-only, preventing self-modification and changes to their roles ([UserAndRoleAssociation]).
    boolean usersSelfEdit();

    /// Defines 2 possible authentication modes – Reduced Sigh-On (RSO) and Single Sign-On (SSO).
    /// Authentication mode is used to guide the login workflow.
    enum AuthMode {
        RSO, SSO;
    }
}
