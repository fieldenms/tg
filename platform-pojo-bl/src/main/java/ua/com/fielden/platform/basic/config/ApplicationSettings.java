package ua.com.fielden.platform.basic.config;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import jakarta.inject.Singleton;

import java.io.File;

/**
 * Default implementation of the contract for generally used in the application settings.
 * 
 * @author TG Team
 * 
 */
@Singleton
public class ApplicationSettings implements IApplicationSettings {

    private static final int DEFAULT_EXTERNAL_SITE_EXPIRY_DAYS = 183;

    private final String appName;
    private final String pathToStorage;
    private final String classPath;
    private final String packagePath;
    private final String pathToSecurityTokens;
    private final String securityTokensPackageName;
    private final Workflows workflow;
    private final AuthMode authMode;
    private final String smtpServer;
    private final String fromAddress;
    private final String currencySymbol;

    @Inject
    protected ApplicationSettings(
            final @Named("app.name") String appName,
            final @Named("reports.path") String pathToStorage,
            final @Named("domain.path") String classPath,
            final @Named("domain.package") String packagePath,
            final @Named("tokens.path") String pathToSecurityTokens,
            final @Named("tokens.package") String securityTokensPackageName,
            final @Named("workflow") String workflow,
            final @Named("auth.mode") String authMode,
            final @Named("email.smtp") String smtpServer,
            final @Named("email.fromAddress") String fromAddress,
            final @Named("currency.symbol") String currencySymbol)
    {
        this.appName = appName;
        this.pathToStorage = prepareSettings(pathToStorage);
        this.classPath = classPath;
        this.packagePath = packagePath;
        this.pathToSecurityTokens = pathToSecurityTokens;
        this.securityTokensPackageName = securityTokensPackageName;
        this.workflow = Workflows.valueOf(workflow.toLowerCase());
        this.authMode = AuthMode.valueOf(authMode.toUpperCase());
        this.smtpServer = smtpServer;
        this.fromAddress = fromAddress;
        this.currencySymbol = currencySymbol;
    }

    @Override
    public String classPath() {
        return classPath;
    }

    @Override
    public String packageName() {
        return packagePath;
    }

    @Override
    public String pathToStorage() {
        return pathToStorage;
    }

    @Override
    public String pathToSecurityTokens() {
        return pathToSecurityTokens;
    }

    @Override
    public String securityTokensPackageName() {
        return securityTokensPackageName;
    }

    @Override
    public String currencySymbol() {
        return currencySymbol;
    }

    @Override
    public String pathToStorageFor(final Class<?> type) {
        return pathToStorage + type.getSimpleName() + "_autocompleters" + System.getProperty("file.separator");
    }

    @Override
    public String appHome() {
        return appName;
    }

    /** A helper method for correct processing of user home portion specified in the path. */
    public String prepareSettings(final String pathToStoreReportSettings) {
        String reportsPath = pathToStoreReportSettings;
        if (reportsPath.startsWith("~")) {
            reportsPath = System.getProperty("user.home") + reportsPath.substring(1);
        }

        if (!reportsPath.endsWith(File.separator)) {
            reportsPath = reportsPath + File.separator;
        }

        final File dir = new File(reportsPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return reportsPath;
    }

    @Override
    public Workflows workflow() {
        return workflow;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    @Override
    public AuthMode authMode() {
        return authMode;
    }

}
