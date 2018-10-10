package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link IEntityEditAction}.
 * 
 * @author Developers
 *
 */
@EntityType(EntityEditAction.class)
public class EntityEditActionDao extends CommonEntityDao<EntityEditAction> implements IEntityEditAction {
    
    @Inject
    public EntityEditActionDao(final IFilter filter) {
        super(filter);
    }
    
}