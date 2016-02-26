package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgFetchProviderTestEntity;
import ua.com.fielden.platform.sample.domain.ITgFetchProviderTestEntity;

/** 
 * Mixin implementation for companion object {@link ITgFetchProviderTestEntity}.
 * 
 * @author Developers
 *
 */
public class TgFetchProviderTestEntityMixin {
    
    private final ITgFetchProviderTestEntity companion;
    
    public TgFetchProviderTestEntityMixin(final ITgFetchProviderTestEntity companion) {
        this.companion = companion;
    }
    
}