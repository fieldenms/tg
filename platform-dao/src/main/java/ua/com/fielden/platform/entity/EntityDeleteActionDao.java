package ua.com.fielden.platform.entity;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.dao.exceptions.EntityCompanionException;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

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
        final List<AbstractEntity<?>> selectedEntities = entity.getContext().getSelectedEntities();
        if (selectedEntities.size() > 0) {
            final Class<? extends AbstractEntity<?>> entityType = selectedEntities.get(0).getType();
            final IEntityDao<? extends AbstractEntity<?>> co = coFinder.find(entityType);
            final List<Long> ids = new ArrayList<Long>();
            selectedEntities.forEach(selectedEntity -> ids.add(selectedEntity.getId()));
            co.batchDelete(ids);
        } else {
            throw new EntityCompanionException("Please select at least one entity to delete.");
        }
        return entity;
    }
}