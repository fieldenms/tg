package ua.com.fielden.platform.security.user;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanSave_Token;

import java.util.Set;

import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKeyFor;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;

/// DAO implementation of the [UserAndRoleAssociationCo]
///
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao extends CommonEntityDao<UserAndRoleAssociation> implements UserAndRoleAssociationCo {

    @Override
    public UserAndRoleAssociation new_() {
        return super.new_().setActive(true);
    }

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanSave_Token.class)
    public UserAndRoleAssociation save(UserAndRoleAssociation entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanSave_Token.class)
    public void deactivateAssociation(final Set<UserAndRoleAssociation> associations) {
        final var co$ = co$(UserAndRoleAssociation.class);
        createQueryByKeyFor(getDbVersion(), getEntityType(), getKeyType(), associations)
        .map(q -> co$.getAllEntities(from(q).with(FETCH_PROVIDER.fetchModel()).model()))
        .ifPresent(toDeactivate -> toDeactivate.forEach(assoc -> co$.save(assoc.setActive(false))));
    }

    @Override
    public IFetchProvider<UserAndRoleAssociation> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
