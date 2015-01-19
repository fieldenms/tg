package ua.com.fielden.platform.sample.domain.mixin;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.sample.domain.TgPersistentEntityWithMoney;
import ua.com.fielden.platform.sample.domain.ITgPersistentEntityWithMoney;

/** 
 * Mixin implementation for companion object {@link ITgPersistentEntityWithMoney}.
 * 
 * @author Developers
 *
 */
public class TgPersistentEntityWithMoneyMixin {
    
    private final ITgPersistentEntityWithMoney companion;
    
    public TgPersistentEntityWithMoneyMixin(final ITgPersistentEntityWithMoney companion) {
        this.companion = companion;
    }
    
}