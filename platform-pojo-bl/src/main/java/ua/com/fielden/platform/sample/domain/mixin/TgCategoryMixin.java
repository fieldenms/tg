package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.ITgCategory;

/** 
 * Mixin implementation for companion object {@link ITgCategory}.
 * 
 * @author Developers
 *
 */
public class TgCategoryMixin {
    
    private final ITgCategory companion;
    
    public TgCategoryMixin(final ITgCategory companion) {
        this.companion = companion;
    }
    
}