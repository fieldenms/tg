package ua.com.fielden.platform.entity;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

import com.google.inject.Inject;

import ua.com.fielden.platform.companion.IPersistentEntityDeleter;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link EntityDeleteActionCo}.
 *
 * @author TG Team
 *
 */
@EntityType(EntityDeleteAction.class)
public class EntityDeleteActionDao extends CommonEntityDao<EntityDeleteAction> implements EntityDeleteActionCo {
    
    @Inject
    public EntityDeleteActionDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    public EntityDeleteAction save(final EntityDeleteAction entity) {
        if (!entity.getSelectedEntityIds().isEmpty()) {
            final IPersistentEntityDeleter<?> deleter = ofNullable(co(entity.getEntityType()))
                    .filter(co -> co instanceof IPersistentEntityDeleter).map(co -> (IPersistentEntityDeleter<?>) co)
                    .orElseThrow(() -> new EntityCompanionException(format("Companion for entity type [%s] does not support deletion inherently.", entity.getEntityType().getSimpleName())));
            deleter.batchDelete(entity.getSelectedEntityIds());
        } else {
            throw new EntityCompanionException("Please select at least one entity to delete.");
        }
        return entity;
    }
    
}