package ua.com.fielden.platform.test.ioc;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import jakarta.inject.Singleton;
import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.basic.config.Workflows;

import java.io.File;
import java.nio.file.FileSystems;

/// A testing implementation of [IApplicationSettings] that allows mutable configuration of all settings per test method.
///
@Singleton
public class ApplicationSettingsForTesting implements IApplicationSettings {

    private String appHome;
    private String classPath;
    private String packageName;
    private String pathToStorage;
    private String pathToSecurityTokens;
    private String securityTokensPackageName;
    private String currencySymbol;
    private Workflows workflow;
    private AuthMode authMode;
    private boolean usersSelfEdit;

    @Inject
    protected ApplicationSettingsForTesting(
            final @Named("app.name") String appName,
            final @Named("reports.path") String pathToStorage,
            final @Named("domain.path") String classPath,
            final @Named("domain.package") String packageName,
            final @Named("tokens.path") String pathToSecurityTokens,
            final @Named("tokens.package") String securityTokensPackageName,
            final @Named("workflow") String workflow,
            final @Named("auth.mode") String authMode,
            final @Named("currency.symbol") String currencySymbol,
            final @Named("users.selfEdit") String usersSelfEdit)
    {
        this.appHome = appName;
        this.classPath = classPath;
        this.packageName = packageName;
        this.pathToStorage = prepareStorage(pathToStorage);
        this.pathToSecurityTokens = pathToSecurityTokens;
        this.securityTokensPackageName = securityTokensPackageName;
        this.currencySymbol = currencySymbol;
        this.workflow = Workflows.valueOf(workflow.toLowerCase());
        this.authMode = AuthMode.valueOf(authMode.toUpperCase());
        this.usersSelfEdit = Boolean.parseBoolean(usersSelfEdit);
    }

    private static String prepareStorage(final String path) {
        var result = path;
        if (result.startsWith("~")) {
            result = System.getProperty("user.home") + result.substring(1);
        }
        if (!result.endsWith(File.separator)) {
            result = result + File.separator;
        }
        return result;
    }

    @Override
    public String appHome() {
        return appHome;
    }

    public void setAppHome(final String appHome) {
        this.appHome = appHome;
    }

    @Override
    public String classPath() {
        return classPath;
    }

    public void setClassPath(final String classPath) {
        this.classPath = classPath;
    }

    @Override
    public String packageName() {
        return packageName;
    }

    public void setPackageName(final String packageName) {
        this.packageName = packageName;
    }

    @Override
    public String pathToStorage() {
        return pathToStorage;
    }

    public void setPathToStorage(final String pathToStorage) {
        this.pathToStorage = pathToStorage;
    }

    @Override
    public String pathToStorageFor(final Class<?> type) {
        return pathToStorage + type.getSimpleName() + "_autocompleters" + FileSystems.getDefault().getSeparator();
    }

    @Override
    public String pathToSecurityTokens() {
        return pathToSecurityTokens;
    }

    public void setPathToSecurityTokens(final String pathToSecurityTokens) {
        this.pathToSecurityTokens = pathToSecurityTokens;
    }

    @Override
    public String securityTokensPackageName() {
        return securityTokensPackageName;
    }

    public void setSecurityTokensPackageName(final String securityTokensPackageName) {
        this.securityTokensPackageName = securityTokensPackageName;
    }

    @Override
    public String currencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(final String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    @Override
    public Workflows workflow() {
        return workflow;
    }

    public void setWorkflow(final Workflows workflow) {
        this.workflow = workflow;
    }

    @Override
    public AuthMode authMode() {
        return authMode;
    }

    public void setAuthMode(final AuthMode authMode) {
        this.authMode = authMode;
    }

    @Override
    public boolean usersSelfEdit() {
        return usersSelfEdit;
    }

    public void setUsersSelfEdit(final boolean usersSelfEdit) {
        this.usersSelfEdit = usersSelfEdit;
    }

}