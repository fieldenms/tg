package ua.com.fielden.platform.serialisation.jackson.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.serialisation.jackson.EntityTypeInfo;
import ua.com.fielden.platform.serialisation.jackson.IEntityTypeInfo;

/** 
 * Mixin implementation for companion object {@link IEntityTypeInfo}.
 * 
 * @author Developers
 *
 */
public class EntityTypeInfoMixin {
    
    private final IEntityTypeInfo companion;
    
    public EntityTypeInfoMixin(final IEntityTypeInfo companion) {
        this.companion = companion;
    }
    
}