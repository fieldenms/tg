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
import ua.com.fielden.platform.sample.domain.mixin.TgAuthorMixin;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import com.google.inject.Inject;

/** 
 * DAO implementation for companion object {@link ITgAuthor}.
 * 
 * @author Developers
 *
 */
@EntityType(TgAuthor.class)
public class TgAuthorDao extends CommonEntityDao<TgAuthor> implements ITgAuthor {
    
    private final TgAuthorMixin mixin;
    
    @Inject
    public TgAuthorDao(final IFilter filter) {
        super(filter);
        
        mixin = new TgAuthorMixin(this);
    }
    
    @Override
    @SessionRequired
    public void delete(final TgAuthor entity) {
        defaultDelete(entity);
    }
    
    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgAuthor> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }
    
}