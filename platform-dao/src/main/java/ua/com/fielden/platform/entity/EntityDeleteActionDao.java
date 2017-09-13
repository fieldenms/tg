package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link IEntityDeleteAction}.
 *
 * @author Developers
 *
 */
@EntityType(EntityDeleteAction.class)
public class EntityDeleteActionDao extends CommonEntityDao<EntityDeleteAction> implements IEntityDeleteAction {
    
    @Inject
    public EntityDeleteActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public EntityDeleteAction save(final EntityDeleteAction entity) {
        if (!entity.getSelectedEntityIds().isEmpty()) {
            co$((Class<? extends AbstractEntity<?>>) entity.getEntityType()).batchDelete(entity.getSelectedEntityIds());
        } else {
            throw new EntityCompanionException("Please select at least one entity to delete.");
        }
        return entity;
    }
    
}