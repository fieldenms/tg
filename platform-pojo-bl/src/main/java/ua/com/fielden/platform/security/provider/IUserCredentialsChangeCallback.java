package ua.com.fielden.platform.security.provider;

/**
 * A contract to be implemented for providing feedback upon changes to username and private key.
 * <p>
 * For example, when user name or private key changes, RestClientUril needs to be updated.
 *
 * @author TG Team
 *
 */
public interface IUserCredentialsChangeCallback {
    void changed(final String username, final String privateKey);
}
