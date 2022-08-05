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

/**
 * DbDriven implementation of the {@link UserAndRoleAssociationCo}
 * 
 * @author TG Team
 * 
 */
@EntityType(UserAndRoleAssociation.class)
public class UserAndRoleAssociationDao extends CommonEntityDao<UserAndRoleAssociation> implements UserAndRoleAssociationCo {

    @Inject
    protected UserAndRoleAssociationDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void removeAssociation(final Set<UserAndRoleAssociation> associations) {
        createQueryByKeyFor(getDbVersion(), getEntityType(), getKeyType(), associations).map(this::batchDelete);
    }
    
    @Override
    @SessionRequired
    public int batchDelete(final EntityResultQueryModel<UserAndRoleAssociation> model) {
        return defaultBatchDelete(model);
    }

    @Override
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    public IFetchProvider<UserAndRoleAssociation> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}