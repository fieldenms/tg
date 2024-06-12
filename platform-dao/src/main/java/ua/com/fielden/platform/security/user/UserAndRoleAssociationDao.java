package ua.com.fielden.platform.security.user;

import static ua.com.fielden.platform.companion.helper.KeyConditionBuilder.createQueryByKeyFor;

import java.util.Collection;
import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.security.Authorise;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanDelete_Token;
import ua.com.fielden.platform.security.tokens.user.UserAndRoleAssociation_CanSave_Token;

/**
 * DbDriven implementation of the {@link UserAndRoleAssociationCo}
 *
 * @author TG Team
 */
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao extends CommonEntityDao<UserAndRoleAssociation> implements UserAndRoleAssociationCo {

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanSave_Token.class)
    public UserAndRoleAssociation save(UserAndRoleAssociation entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanDelete_Token.class)
    public void removeAssociation(final Set<UserAndRoleAssociation> associations) {
        createQueryByKeyFor(getDbVersion(), getEntityType(), getKeyType(), associations).map(this::batchDelete);
    }

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanDelete_Token.class)
    public int batchDelete(final EntityResultQueryModel<UserAndRoleAssociation> model) {
        return defaultBatchDelete(model);
    }

    @Override
    @SessionRequired
    @Authorise(UserAndRoleAssociation_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    public IFetchProvider<UserAndRoleAssociation> createFetchProvider() {
        return FETCH_PROVIDER;
    }

}
