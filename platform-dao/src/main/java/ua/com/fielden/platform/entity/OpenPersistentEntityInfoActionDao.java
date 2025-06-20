package ua.com.fielden.platform.entity;

import com.google.inject.Inject;
import ua.com.fielden.platform.dao.AbstractOpenCompoundMasterDao;
import ua.com.fielden.platform.dao.IEntityAggregatesOperations;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/// DAO implementation for companion object {@link OpenPersistentEntityInfoAction}.
///
@EntityType(OpenPersistentEntityInfoAction.class)
public class OpenPersistentEntityInfoActionDao extends AbstractOpenCompoundMasterDao<OpenPersistentEntityInfoAction>  implements OpenPersistentEntityInfoActionCo {

    @Inject
    public OpenPersistentEntityInfoActionDao(final IFilter filter, final IEntityAggregatesOperations coAggregates) {
        super(filter, coAggregates);
    }

    @Override
    protected IFetchProvider<OpenPersistentEntityInfoAction> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}
