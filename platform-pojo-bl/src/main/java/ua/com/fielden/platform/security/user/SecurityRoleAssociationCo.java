package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.security.ISecurityToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/// Interface that defines the API for retrieving saving and removing the [SecurityRoleAssociation] instances.
///
public interface SecurityRoleAssociationCo extends IEntityDao<SecurityRoleAssociation> {

    /// Returns the list of [SecurityRoleAssociation] those are associated with given security token
    ///
    List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken);

    /// Returns the map between security tokens and set of associated user roles.
    ///
    Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations();

    /// Returns a total number of active user roles associated with the token.
    ///
    int countActiveAssociations(final User user, final Class<? extends ISecurityToken> securityTokenClass);
    
    /// Deletes a collection of [SecurityRoleAssociation]s.
    ///
    void removeAssociations(final Collection<SecurityRoleAssociation> associations);

}
