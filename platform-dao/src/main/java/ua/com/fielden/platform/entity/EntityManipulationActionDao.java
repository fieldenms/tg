package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.swing.review.annotations.EntityType;

import com.google.inject.Inject;

/**
 * DAO implementation for companion object {@link IEntityManipulationAction}.
 *
 * @author Developers
 *
 */
@EntityType(EntityManipulationAction.class)
public class EntityManipulationActionDao extends CommonEntityDao<EntityManipulationAction> implements IEntityManipulationAction {
    @Inject
    public EntityManipulationActionDao(final IFilter filter) {
        super(filter);
    }

    @Override
    public EntityManipulationAction save(final EntityManipulationAction entity) {
        return super.save(entity);
    }

}