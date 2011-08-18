package ua.com.fielden.platform.basic.config;

import java.io.File;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * Default implementation of the contract for generally used in the application settings.
 *
 * @author TG Team
 *
 */
public class ApplicationSettings implements IApplicationSettings {
    private final String appHome;
    private final String pathToStorage;
    private final String classPath;
    private final String packagePath;
    private final String pathToSecurityTokens;
    private final String securityTokensPackageName;
    private final String privateKey;
    private final String workflow;

    @Inject
    protected ApplicationSettings(//
	    final @Named("app.home") String appHome, //
	    final @Named("reports.path") String pathToStorage, //
	    final @Named("domain.path") String classPath,//
	    final @Named("domain.package") String packagePath,//
	    final @Named("tokens.path") String pathToSecurityTokens,//
	    final @Named("tokens.package") String securityTokensPackageName,//
	    final @Named("private-key") String privateKey,//
	    final @Named("workflow") String workflow) {
	this.appHome = prepareSettings(appHome);
	this.pathToStorage = prepareSettings(pathToStorage);
	this.classPath = classPath;
	this.packagePath = packagePath;
	this.pathToSecurityTokens = pathToSecurityTokens;
	this.securityTokensPackageName = securityTokensPackageName;
	this.privateKey = privateKey;
	this.workflow = workflow;
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
    public String pathToStorageFor(final Class<?> type) {
	return pathToStorage + type.getSimpleName() + "_autocompleters" + System.getProperty("file.separator");
    }

    @Override
    public String appHome() {
	return appHome;
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
    public String privateKey() {
	return privateKey;
    }

    @Override
    public String workflow() {
	return workflow;
    }

}
