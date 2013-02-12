package ua.com.fielden.platform.client.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

/**
 * A class responsible for persisting and invalidating of user sessions. It is used for emulating single sign on by means of remembering user's private key and name used during the
 * last successful login.
 *
 * @author TG Team
 *
 */
public class AppSessionController {
    public static final String USERNAME = "username";
    public static final String PRIVATE_KEY = "private-key";

    private final String path;
    private final String dirs;
    private final Properties properties = new Properties();

    private String username;
    private String privateKey;
    private final String applicationWidePrivateKey;

    public AppSessionController(final String pathToConfigFile, final String applicationWidePrivateKey) throws Exception {
	path = pathToConfigFile;
	dirs = pathToConfigFile.lastIndexOf("/") > 0 ? pathToConfigFile.substring(0, pathToConfigFile.lastIndexOf("/")) : null;
	final File file = new File(path);
	if (file.exists()) {
	    try (final InputStream is = new FileInputStream(path)) {
		properties.load(is);
	    }
	}

	this.applicationWidePrivateKey = applicationWidePrivateKey;
	username = properties.getProperty(AppSessionController.USERNAME);
	privateKey = properties.getProperty(AppSessionController.PRIVATE_KEY);
    }

    public void persist(final String username, final String privateKey) throws Exception {
	setUsername(username);
	setPrivateKey(privateKey);
	properties.setProperty(USERNAME, getUsername());
	properties.setProperty(PRIVATE_KEY, getPrivateKey());

	final File file = new File(path);
	if (!file.exists()) {
	    if (dirs != null) {
		final File dirFile = new File(dirs);
		dirFile.mkdirs();
	    }
	    file.createNewFile();
	}
	properties.store(new FileOutputStream(path), "client application settings");
    }

    /**
     * Deletes a file with session information, which basically invalidates the session.
     */
    public void remove() {
	final File file = new File(path);
	if (file.exists()) {
	    file.delete();
	}
    }

    public boolean areUserSettingsPresent() {
	return !StringUtils.isEmpty(properties.getProperty(USERNAME)) && !StringUtils.isEmpty(properties.getProperty(PRIVATE_KEY));
    }

    public String getApplicationWidePrivateKey() {
	return applicationWidePrivateKey;
    }

    public String getUsername() {
	return username;
    }

    /**
     * Assigns username. Null value is not assigned and silently ignored.
     *
     * @param username
     */
    public void setUsername(final String username) {
	this.username = username != null ? username : this.username;
    }

    public String getPrivateKey() {
	return privateKey;
    }

    /**
     * Assigns private key. Null value is not assigned and silently ignored.
     *
     * @param privateKey
     */
    public void setPrivateKey(final String privateKey) {
	this.privateKey = privateKey != null ? privateKey : this.privateKey;
    }

    /** Resets username and private key by assigning null values. */
    public void resetUser() {
	privateKey = null;
	username = null;
    }
}
