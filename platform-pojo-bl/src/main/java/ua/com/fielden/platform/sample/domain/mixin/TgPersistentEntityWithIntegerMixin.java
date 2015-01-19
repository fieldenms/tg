package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithInteger;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithInteger;

/** 
 * Mixin implementation for companion object {@link ITgPersistentEntityWithInteger}.
 * 
 * @author Developers
 *
 */
public class TgPersistentEntityWithIntegerMixin {
    
    private final ITgPersistentEntityWithInteger companion;
    
    public TgPersistentEntityWithIntegerMixin(final ITgPersistentEntityWithInteger companion) {
        this.companion = companion;
    }
    
}