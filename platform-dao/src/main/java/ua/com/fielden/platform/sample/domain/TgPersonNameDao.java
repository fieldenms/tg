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
import ua.com.fielden.platform.sample.domain.mixin.TgPersonNameMixin;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgPersonName}.
 * 
 * @author Developers
 *
 */
@EntityType(TgPersonName.class)
public class TgPersonNameDao extends CommonEntityDao<TgPersonName> implements ITgPersonName {
    
    private final TgPersonNameMixin mixin;
    
    @Inject
    public TgPersonNameDao(final IFilter filter) {
        super(filter);
        
        mixin = new TgPersonNameMixin(this);
    }
    
    @Override
    @SessionRequired
    public void delete(final TgPersonName entity) {
        defaultDelete(entity);
    }
    
    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgPersonName> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
    
}