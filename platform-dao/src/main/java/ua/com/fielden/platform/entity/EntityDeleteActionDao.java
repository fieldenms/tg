package ua.com.fielden.platform.entity;

import java.util.Set;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link IEntityDeleteAction}.
 *
 * @author Developers
 *
 */
@EntityType(EntityDeleteAction.class)
public class EntityDeleteActionDao extends CommonEntityDao<EntityDeleteAction> implements IEntityDeleteAction {

    private final ICompanionObjectFinder coFinder;

    @Inject
    public EntityDeleteActionDao(final IFilter filter, final ICompanionObjectFinder coFinder) {
        super(filter);
        this.coFinder = coFinder;
    }

    @Override
    public EntityDeleteAction save(final EntityDeleteAction entity) {
        final Set<Long> selectedEntityIds = entity.getSelectedEntityIds();
        if (selectedEntityIds.size() > 0) {
            final Class<? extends AbstractEntity<?>> entityType = (Class<? extends AbstractEntity<?>>) entity.getEntityType();
            final IEntityDao<? extends AbstractEntity<?>> co = coFinder.find(entityType);
            co.batchDelete(selectedEntityIds);
        } else {
            throw new EntityCompanionException("Please select at least one entity to delete.");
        }
        return entity;
    }
}