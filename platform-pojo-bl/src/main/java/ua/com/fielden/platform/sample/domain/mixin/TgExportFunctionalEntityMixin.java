package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgExportFunctionalEntity;
import ua.com.fielden.platform.sample.domain.ITgExportFunctionalEntity;

/** 
 * Mixin implementation for companion object {@link ITgExportFunctionalEntity}.
 * 
 * @author Developers
 *
 */
public class TgExportFunctionalEntityMixin {
    
    private final ITgExportFunctionalEntity companion;
    
    public TgExportFunctionalEntityMixin(final ITgExportFunctionalEntity companion) {
        this.companion = companion;
    }
    
}