package ua.com.fielden.platform.security.user;

import java.util.List;
import java.util.Map;
import java.util.Set;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.ISecurityToken;

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

    /** Returns a total number of active user roles associated with the token. */
    int countActiveAssociations(final User user, final Class<? extends ISecurityToken> securityTokenClass);
    
    /**
     * Removes the set of {@link SecurityRoleAssociation}s.
     * 
     * @param associations
     */
    void removeAssociations(final Set<SecurityRoleAssociation> associations);
}
