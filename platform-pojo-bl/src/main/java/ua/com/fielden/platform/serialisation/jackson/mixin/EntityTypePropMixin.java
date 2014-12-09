package ua.com.fielden.platform.serialisation.jackson.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeProp;
import ua.com.fielden.platform.serialisation.jackson.IEntityTypeProp;

/** 
 * Mixin implementation for companion object {@link IEntityTypeProp}.
 * 
 * @author Developers
 *
 */
public class EntityTypePropMixin {
    
    private final IEntityTypeProp companion;
    
    public EntityTypePropMixin(final IEntityTypeProp companion) {
        this.companion = companion;
    }
    
}