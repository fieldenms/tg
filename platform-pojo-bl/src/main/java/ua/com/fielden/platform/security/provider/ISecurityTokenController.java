package ua.com.fielden.platform.security.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * Controller interface that provides a contract for retrieving and saving user roles associated with security tokens.
 * 
 * @author TG Team
 * 
 */
public interface ISecurityTokenController {

    /**
     * Returns the map between security tokens and set of associated user roles.
     * 
     * @param securityToken
     * @return
     */
    Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations();

    /**
     * Returns the set of {@link UserRole} associated with specified {@link ISecurityToken}
     * 
     * @param securityTokenClass
     * @return
     */
    List<UserRole> findUserRolesFor(final Class<? extends ISecurityToken> securityTokenClass);

    /** Checks whether the passed in user and token are associated, indicating ability for the user to access annotated with this token methods. */
    boolean canAccess(final User user, final Class<? extends ISecurityToken> securityTokenClass);

}