package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.sample.domain.ITgFunctionalEntityWithCentreContext;

/** 
 * Mixin implementation for companion object {@link ITgFunctionalEntityWithCentreContext}.
 * 
 * @author Developers
 *
 */
public class TgFunctionalEntityWithCentreContextMixin {
    
    private final ITgFunctionalEntityWithCentreContext companion;
    
    public TgFunctionalEntityWithCentreContextMixin(final ITgFunctionalEntityWithCentreContext companion) {
        this.companion = companion;
    }
    
}