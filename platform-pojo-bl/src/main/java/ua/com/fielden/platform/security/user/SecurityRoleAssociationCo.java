package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.utils.EntityUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.security.user.User.EMAIL;
import static ua.com.fielden.platform.security.user.User.SSO_ONLY;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;

/// Interface that defines the API for retrieving saving and removing the [SecurityRoleAssociation] instances.
///
public interface SecurityRoleAssociationCo extends IEntityDao<SecurityRoleAssociation> {

    static final IFetchProvider<SecurityRoleAssociation> FETCH_PROVIDER = EntityUtils.fetch(SecurityRoleAssociation.class)
            .with("securityToken", "role", "active");

    static fetch<SecurityRoleAssociation> FETCH_MODEL = FETCH_PROVIDER.fetchModel();

    /// Returns the list of [SecurityRoleAssociation] those are associated with given security token
    ///
    List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken);

    /// Returns the map between security tokens and set of associated user roles.
    ///
    Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations();

    /// Returns a total number of active user roles associated with the token.
    ///
    int countActiveAssociations(final User user, final Class<? extends ISecurityToken> securityTokenClass);

    /// Returns a list of active user roles associated with the tokens.
    ///
    List<SecurityRoleAssociation> findActiveAssociationsForUser(final User user, final Class<? extends ISecurityToken>... tokens);
    
    /// Deletes a collection of [SecurityRoleAssociation]s.
    ///
    void removeAssociations(final Collection<SecurityRoleAssociation> associations);

    /// Creates and saves all associations in the stream.
    ///
    void addAssociations(Collection<SecurityRoleAssociation> associations);

}
