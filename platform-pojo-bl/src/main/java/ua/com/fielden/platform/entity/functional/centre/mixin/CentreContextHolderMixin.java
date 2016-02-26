package ua.com.fielden.platform.entity.functional.centre.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.ICentreContextHolder;

/** 
 * Mixin implementation for companion object {@link ICentreContextHolder}.
 * 
 * @author Developers
 *
 */
public class CentreContextHolderMixin {
    
    private final ICentreContextHolder companion;
    
    public CentreContextHolderMixin(final ICentreContextHolder companion) {
        this.companion = companion;
    }
    
}