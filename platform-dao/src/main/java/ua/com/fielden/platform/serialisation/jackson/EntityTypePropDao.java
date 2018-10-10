package ua.com.fielden.platform.serialisation.jackson;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link IEntityTypeProp}.
 * 
 * @author TG Team
 *
 */
@EntityType(EntityTypeProp.class)
public class EntityTypePropDao extends CommonEntityDao<EntityTypeProp> implements IEntityTypeProp {
    
    @Inject
    public EntityTypePropDao(final IFilter filter) {
        super(filter);
    }
    
}