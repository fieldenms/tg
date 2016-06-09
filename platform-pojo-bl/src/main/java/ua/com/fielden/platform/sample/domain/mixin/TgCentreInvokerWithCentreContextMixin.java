package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgCentreInvokerWithCentreContext;
import ua.com.fielden.platform.sample.domain.ITgCentreInvokerWithCentreContext;

/** 
 * Mixin implementation for companion object {@link ITgCentreInvokerWithCentreContext}.
 * 
 * @author Developers
 *
 */
public class TgCentreInvokerWithCentreContextMixin {
    
    private final ITgCentreInvokerWithCentreContext companion;
    
    public TgCentreInvokerWithCentreContextMixin(final ITgCentreInvokerWithCentreContext companion) {
        this.companion = companion;
    }
    
}