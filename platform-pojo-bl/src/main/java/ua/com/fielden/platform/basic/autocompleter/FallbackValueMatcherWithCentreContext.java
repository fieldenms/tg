package ua.com.fielden.platform.basic.autocompleter;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.web.centre.CentreContext;

/**
 * This is a fall back implementation for {@link IValueMatcherWithCentreContext}, which does not do anything with the provided context.
 * It simply performs the search by key operation.
 *
 * @author TG Team
 *
 * @param <T>
 */
public class FallbackValueMatcherWithCentreContext<T extends AbstractEntity<?>> extends AbstractSearchEntityByKeyWithCentreContext<T> {

    public FallbackValueMatcherWithCentreContext(final IEntityDao<T> dao) {
        super(dao);
    }

    @Override
    public void setContext(final CentreContext<T, ?> context) {

    }

    @Override
    public CentreContext<T, ?> getContext() {
        return null;
    }

    @Override
    public Integer getPageSize() {
        return 10;
    }

    @Override
    protected EntityResultQueryModel<T> completeEqlBasedOnContext(
            final CentreContext<T, ?> context,
            final String searchString,
            final ICompoundCondition0<T> incompleteEql) {
        return incompleteEql.model();
    }

}
