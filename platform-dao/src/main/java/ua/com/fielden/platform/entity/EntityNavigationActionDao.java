package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(EntityNavigationAction.class)
public class EntityNavigationActionDao extends CommonEntityDao<EntityNavigationAction> implements IEntityNavigationAction {

    @Inject
    protected EntityNavigationActionDao(final IFilter filter) {
        super(filter);
    }
}
