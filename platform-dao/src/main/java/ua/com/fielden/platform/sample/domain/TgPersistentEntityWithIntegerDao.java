package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import java.util.Map;
import ua.com.fielden.platform.pagination.IPage;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.sample.domain.mixin.TgPersistentEntityWithIntegerMixin;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgPersistentEntityWithInteger}.
 * 
 * @author Developers
 *
 */
@EntityType(TgPersistentEntityWithInteger.class)
public class TgPersistentEntityWithIntegerDao extends CommonEntityDao<TgPersistentEntityWithInteger> implements ITgPersistentEntityWithInteger {
    
    private final TgPersistentEntityWithIntegerMixin mixin;
    
    @Inject
    public TgPersistentEntityWithIntegerDao(final IFilter filter) {
        super(filter);
        
        mixin = new TgPersistentEntityWithIntegerMixin(this);
    }
    
}