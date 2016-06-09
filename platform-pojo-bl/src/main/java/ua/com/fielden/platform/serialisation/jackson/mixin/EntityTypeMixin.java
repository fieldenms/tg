package ua.com.fielden.platform.serialisation.jackson.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.serialisation.jackson.EntityType;
import ua.com.fielden.platform.serialisation.jackson.IEntityType;

/** 
 * Mixin implementation for companion object {@link IEntityType}.
 * 
 * @author Developers
 *
 */
public class EntityTypeMixin {
    
    private final IEntityType companion;
    
    public EntityTypeMixin(final IEntityType companion) {
        this.companion = companion;
    }
    
}