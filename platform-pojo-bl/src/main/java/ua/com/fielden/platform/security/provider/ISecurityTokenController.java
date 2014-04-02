package ua.com.fielden.platform.security.provider;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.security.ISecurityToken;
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

    /**
     * Override it to provide the ability to persist the {@link UserRole}s associated with {@link ISecurityToken}s.
     * 
     * @param tokenToRoleAssocations
     *            -- a map between tokens and roles that need to be associated
     */
    void saveSecurityToken(final Map<Class<? extends ISecurityToken>, Set<UserRole>> tokenToRoleAssocations);

    /**
     * Returns all distinct {@link UserRole}s (It is needed for building SecurityTreeTableModel)
     * 
     * @return
     */
    List<UserRole> findUserRoles();

    /** Checks whether the passed in user and token are associated, indicating ability for the user to access annotated with this token methods. */
    boolean canAccess(final String username, final Class<? extends ISecurityToken> securityTokenClass);
}
