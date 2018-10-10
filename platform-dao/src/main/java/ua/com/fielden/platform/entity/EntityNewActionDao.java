package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link IEntityNewAction}.
 * 
 * @author Developers
 *
 */
@EntityType(EntityNewAction.class)
public class EntityNewActionDao extends CommonEntityDao<EntityNewAction> implements IEntityNewAction {
    
    @Inject
    public EntityNewActionDao(final IFilter filter) {
        super(filter);
    }
    
}