package ua.com.fielden.platform.domaintree;

/**
 * This interface defines how to manage {@link IGlobalDomainTreeManager}s for different users on the server. <br>
 *
 * @author TG Team
 *
 */
public interface IServerGlobalDomainTreeManager {
    /**
     * Returns the current version of {@link IGlobalDomainTreeManager} for concrete user.
     *
     * @param username
     * @return
     */
    IGlobalDomainTreeManager get(final String username);
}
