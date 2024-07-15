package ua.com.fielden.platform.security.provider;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.User;

/**
 * Controller interface that provides a contract for retrieving and saving user roles associated with security tokens.
 * 
 * @author TG Team
 * 
 */
public interface ISecurityTokenController {

    /** Checks whether the passed in user and token are associated, indicating ability for the user to access annotated with this token methods. */
    boolean canAccess(final User user, final Class<? extends ISecurityToken> securityTokenClass);

}