package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgEntityWithLoopedCalcProps;
import ua.com.fielden.platform.sample.domain.ITgEntityWithLoopedCalcProps;

/** 
 * Mixin implementation for companion object {@link ITgEntityWithLoopedCalcProps}.
 * 
 * @author Developers
 *
 */
public class TgEntityWithLoopedCalcPropsMixin {
    
    private final ITgEntityWithLoopedCalcProps companion;
    
    public TgEntityWithLoopedCalcPropsMixin(final ITgEntityWithLoopedCalcProps companion) {
        this.companion = companion;
    }
    
}