package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;

import java.util.Set;

import static ua.com.fielden.platform.utils.EntityUtils.fetch;

/// Interface that defines the API for retrieving, saving and removing [UserAndRoleAssociation] instances
///
public interface UserAndRoleAssociationCo extends IEntityDao<UserAndRoleAssociation> {

    IFetchProvider<UserAndRoleAssociation> FETCH_PROVIDER = fetch(UserAndRoleAssociation.class).with("user", "userRole");

    /// Deactivates `associations`.
    ///
    void deactivateAssociation(Set<UserAndRoleAssociation> associations);

}