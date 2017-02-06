package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * This is a fall back implementation for {@link IValueMatcherWithContext}, which does not support context. It simply performs the search by key operation.
 *
 * @author TG Team
 *
 * @param <CONTEXT>
 * @param <T>
 */
public class FallbackValueMatcherWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithContext<CONTEXT, T> {

    private final Class<T> entityType;
    
    public FallbackValueMatcherWithContext(final IEntityDao<T> co) {
        super(co);
        
        entityType = co.getEntityType();
    }

    @Override
    public Integer getPageSize() {
        return IEntityDao.DEFAULT_PAGE_CAPACITY;
    }

    @Override
    protected EntityResultQueryModel<T> completeEqlBasedOnContext(
            final CONTEXT context,
            final String searchString,
            final ICompoundCondition0<T> incompleteEql) {
        if (EntityUtils.hasDescProperty(entityType)) {
            return incompleteEql.or().upperCase().prop(AbstractEntity.DESC).iLike().val("%" + searchString).model();
        } else {
            return incompleteEql.model();
        }
    }

}
