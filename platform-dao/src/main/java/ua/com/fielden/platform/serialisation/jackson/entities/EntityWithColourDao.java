package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.serialisation.jackson.entities.mixin.EntityWithColourMixin;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link IEntityWithColour}.
 * 
 * @author Developers
 *
 */
@EntityType(EntityWithColour.class)
public class EntityWithColourDao extends CommonEntityDao<EntityWithColour> implements IEntityWithColour {
    
    private final EntityWithColourMixin mixin;
    
    @Inject
    public EntityWithColourDao(final IFilter filter) {
        super(filter);
        
        mixin = new EntityWithColourMixin(this);
    }
    
}