package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * This is a fall back implementation for {@link IValueMatcherWithContext}, which does not support context. It simply performs the search by key operation.
 *
 * @author TG Team
 *
 * @param <CONTEXT>
 * @param <T>
 */
public class FallbackValueMatcherWithContext<CONTEXT extends AbstractEntity<?>, T extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithContext<CONTEXT, T> {

    public FallbackValueMatcherWithContext(final IEntityDao<T> dao) {
        super(dao);
    }

    @Override
    public void setContext(final CONTEXT context) {

    }

    @Override
    public CONTEXT getContext() {
        return null;
    }

    @Override
    public Integer getPageSize() {
        return 10;
    }

    @Override
    protected EntityResultQueryModel<T> completeEqlBasedOnContext(
            final CONTEXT context,
            final String searchString,
            final ICompoundCondition0<T> incompleteEql) {
        return incompleteEql.model();
    }

}
