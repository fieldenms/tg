package ua.com.fielden.platform.serialisation.jackson;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.rao.CommonEntityRao;
import ua.com.fielden.platform.rao.RestClientUtil;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.serialisation.jackson.mixin.EntityTypePropMixin;
import com.google.inject.Inject;

/** 
 * RAO implementation for master object {@link IEntityTypeProp} based on a common with DAO mixin.
 * 
 * @author Developers
 *
 */
@EntityType(EntityTypeProp.class)
public class EntityTypePropRao extends CommonEntityRao<EntityTypeProp> implements IEntityTypeProp {

    
    private final EntityTypePropMixin mixin;
    
    @Inject
    public EntityTypePropRao(final RestClientUtil restUtil) {
        super(restUtil);
        
        mixin = new EntityTypePropMixin(this);
    }
    
}