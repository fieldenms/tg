package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.error.Result;

/**
 * This contract provides an abstraction for user authentication process based on <code>username</code> and <code>password</code>.
 * It can be bound to an application specific implementation in the IoC module and provides a way to support LDAP driven or other customer specific authentication models.
 *
 * @author TG Team
 */
public interface IAuthenticationModel {

    /**
     * This method should return successful {@link Result} with respective {@link User} set as instance<br>
     * OR<br>
     * unsuccessful {@link Result} with null set as instance and error description set as message.
     *
     * @param username
     * @param password
     * @return
     */
    Result authenticate(final String username, final String password);

}
