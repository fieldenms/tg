package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithProperties;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithProperties;

/** 
 * Mixin implementation for companion object {@link ITgPersistentEntityWithProperties}.
 * 
 * @author Developers
 *
 */
public class TgPersistentEntityWithPropertiesMixin {
    
    private final ITgPersistentEntityWithProperties companion;
    
    public TgPersistentEntityWithPropertiesMixin(final ITgPersistentEntityWithProperties companion) {
        this.companion = companion;
    }
    
}