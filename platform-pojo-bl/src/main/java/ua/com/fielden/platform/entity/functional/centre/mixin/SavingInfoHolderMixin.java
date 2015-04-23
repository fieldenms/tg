package ua.com.fielden.platform.entity.functional.centre.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.functional.centre.ISavingInfoHolder;

/** 
 * Mixin implementation for companion object {@link ISavingInfoHolder}.
 * 
 * @author Developers
 *
 */
public class SavingInfoHolderMixin {
    
    private final ISavingInfoHolder companion;
    
    public SavingInfoHolderMixin(final ISavingInfoHolder companion) {
        this.companion = companion;
    }
    
}