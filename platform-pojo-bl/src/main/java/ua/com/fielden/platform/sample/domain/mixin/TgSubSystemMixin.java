package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.ITgSubSystem;

/** 
 * Mixin implementation for companion object {@link ITgSubSystem}.
 * 
 * @author Developers
 *
 */
public class TgSubSystemMixin {
    
    private final ITgSubSystem companion;
    
    public TgSubSystemMixin(final ITgSubSystem companion) {
        this.companion = companion;
    }
    
}