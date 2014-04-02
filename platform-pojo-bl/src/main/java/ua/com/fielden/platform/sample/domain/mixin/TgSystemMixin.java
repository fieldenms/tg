package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgSystem;
import ua.com.fielden.platform.sample.domain.ITgSystem;

/** 
 * Mixin implementation for companion object {@link ITgSystem}.
 * 
 * @author Developers
 *
 */
public class TgSystemMixin {
    
    private final ITgSystem companion;
    
    public TgSystemMixin(final ITgSystem companion) {
        this.companion = companion;
    }
    
}