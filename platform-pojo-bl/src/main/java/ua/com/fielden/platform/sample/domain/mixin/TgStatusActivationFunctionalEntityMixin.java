package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgStatusActivationFunctionalEntity;
import ua.com.fielden.platform.sample.domain.ITgStatusActivationFunctionalEntity;

/** 
 * Mixin implementation for companion object {@link ITgStatusActivationFunctionalEntity}.
 * 
 * @author Developers
 *
 */
public class TgStatusActivationFunctionalEntityMixin {
    
    private final ITgStatusActivationFunctionalEntity companion;
    
    public TgStatusActivationFunctionalEntityMixin(final ITgStatusActivationFunctionalEntity companion) {
        this.companion = companion;
    }
    
}