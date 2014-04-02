package ua.com.fielden.platform.basic.config;

/**
 * A contract for generally used in the application settings.
 * 
 * @author TG Team
 * 
 */
public interface IApplicationSettings {
    String appHome();

    String classPath();

    String packageName();

    String pathToStorage();

    String pathToStorageFor(Class<?> type);

    String pathToSecurityTokens();

    String securityTokensPackageName();

    String privateKey();

    String workflow();
}
