package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.ISecurityToken;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.security.user.SecurityRoleAssociation.ROLE;
import static ua.com.fielden.platform.security.user.SecurityRoleAssociation.SECURITY_TOKEN;
import static ua.com.fielden.platform.utils.EntityUtils.fetch;

/// Interface that defines the API for retrieving saving and removing the [SecurityRoleAssociation] instances.
///
public interface SecurityRoleAssociationCo extends IEntityDao<SecurityRoleAssociation> {

    IFetchProvider<SecurityRoleAssociation> FETCH_PROVIDER = fetch(SecurityRoleAssociation.class)
            .with(SECURITY_TOKEN,
                  ROLE,
                  ACTIVE,
                  // `CREATED_*` properties are needed to save new associations in [CopyUserRoleActionDao].
                  CREATED_BY,
                  CREATED_DATE,
                  CREATED_TRANSACTION_GUID);

    fetch<SecurityRoleAssociation> FETCH_MODEL = FETCH_PROVIDER.fetchModel();

    /// Returns the list of [SecurityRoleAssociation] those are associated with given security token
    ///
    List<SecurityRoleAssociation> findAssociationsFor(final Class<? extends ISecurityToken> securityToken);

    /// Returns the map between security tokens and set of associated user roles.
    ///
    Map<Class<? extends ISecurityToken>, Set<UserRole>> findAllAssociations();

    /// Returns a total number of active user roles associated with the token.
    ///
    int countActiveAssociations(final User user, final Class<? extends ISecurityToken> securityTokenClass);

    /// Selects all active [SecurityRoleAssociation] records for all active roles of `user` associated with any of `tokens`.
    ///
    EntityResultQueryModel<SecurityRoleAssociation> selectActiveAssociations(final User user, final Class<? extends ISecurityToken>... tokens);
    
    /// Deletes a collection of [SecurityRoleAssociation]s.
    ///
    void removeAssociations(final Collection<SecurityRoleAssociation> associations);

    /// Creates or activates all associations in the collection.
    ///
    /// @param associations a collection of instrumented instances, each of which has all required key values and does not have ID
    ///
    void addAssociations(Collection<SecurityRoleAssociation> associations);

}
