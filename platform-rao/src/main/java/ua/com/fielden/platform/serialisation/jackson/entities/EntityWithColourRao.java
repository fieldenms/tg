package ua.com.fielden.platform.serialisation.jackson.entities;

import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.serialisation.jackson.entities.mixin.EntityWithColourMixin;
import com.google.inject.Inject;

/** 
 * RAO implementation for master object {@link IEntityWithColour} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(EntityWithColour.class)
public class EntityWithColourRao extends CommonEntityRao<EntityWithColour> implements IEntityWithColour {

    
    private final EntityWithColourMixin mixin;
    
    @Inject
    public EntityWithColourRao(final RestClientUtil restUtil) {
        super(restUtil);
        
        mixin = new EntityWithColourMixin(this);
    }
    
}