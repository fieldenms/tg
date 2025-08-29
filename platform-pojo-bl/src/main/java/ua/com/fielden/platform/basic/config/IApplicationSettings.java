package ua.com.fielden.platform.basic.config;

/// A contract for generally used in the application settings.
///
/// @author TG Team
public interface IApplicationSettings {
    String appHome();

    String classPath();

    String packageName();

    String pathToStorage();

    String pathToStorageFor(Class<?> type);

    String pathToSecurityTokens();

    String securityTokensPackageName();

    String currency();

    Workflows workflow();
    
    AuthMode authMode();
    
    /// Defines 2 possible authentication modes – Reduced Sigh-On (RSO) and Single Sign-On (SSO).
    /// Authentication mode is used to guide the login workflow.
    public enum AuthMode {
        RSO, SSO;
    }
}