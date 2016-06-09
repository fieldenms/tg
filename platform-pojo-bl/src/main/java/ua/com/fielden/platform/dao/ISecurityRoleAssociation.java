package ua.com.fielden.platform.dao;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserRole;

/**
 * Interface that defines the API for retrieving saving and removing the {@link SecurityRoleAssociation} instances.
 * 
 * @author TG Team
 * 
 */
public interface ISecurityRoleAssociation extends IEntityDao<SecurityRoleAssociation> {

    /**
     * Returns the list of {@link SecurityRoleAssociation} those are associated with given security token
     * 
     * @param securityToken
     * @return
     */
    List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken);

    /**
     * Returns the map between security tokens and set of associated user roles.
     * 
     * @param securityToken
     * @return
     */
    Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations();

    /**
     * Removes all role association with the specified token.
     * 
     * @param association
     */
    void removeAssociationsFor(final Class<? extends ISecurityToken> securityToken);

    /** Returns a total number of active user roles associated with the token. */
    int countActiveAssociations(final User user, final Class<? extends ISecurityToken> securityTokenClass);
    
    /**
     * Removes the set of {@link SecurityRoleAssociation}s.
     * 
     * @param associations
     */
    void removeAssociations(final Set<SecurityRoleAssociation> associations);
}
