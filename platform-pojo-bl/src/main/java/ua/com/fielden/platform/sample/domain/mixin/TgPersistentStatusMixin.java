package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgPersistentStatus;
import ua.com.fielden.platform.sample.domain.ITgPersistentStatus;

/** 
 * Mixin implementation for companion object {@link ITgPersistentStatus}.
 * 
 * @author Developers
 *
 */
public class TgPersistentStatusMixin {
    
    private final ITgPersistentStatus companion;
    
    public TgPersistentStatusMixin(final ITgPersistentStatus companion) {
        this.companion = companion;
    }
    
}