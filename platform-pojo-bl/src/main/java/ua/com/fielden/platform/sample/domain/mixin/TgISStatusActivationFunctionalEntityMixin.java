package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgISStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.ITgISStatusActivationFunctionalEntity;

/** 
 * Mixin implementation for companion object {@link ITgISStatusActivationFunctionalEntity}.
 * 
 * @author Developers
 *
 */
public class TgISStatusActivationFunctionalEntityMixin {
    
    private final ITgISStatusActivationFunctionalEntity companion;
    
    public TgISStatusActivationFunctionalEntityMixin(final ITgISStatusActivationFunctionalEntity companion) {
        this.companion = companion;
    }
    
}