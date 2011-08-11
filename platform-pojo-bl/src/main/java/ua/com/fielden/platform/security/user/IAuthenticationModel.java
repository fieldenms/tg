/**
 *
 */
package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.error.Result;

/**
 * This interface provides abstraction for user authentication
 * 
 * @author TG Team
 */
public interface IAuthenticationModel {

    /**
     * This method should return successful {@link Result} with respective {@link User}(or its subclass, say Person) set as instance<br>
     * OR<br>
     * unsuccessful {@link Result} with null set as instance and error description set as message.
     * 
     * @param username
     * @param password
     * @return
     */
    Result authenticate(final String username, final String password);

}
