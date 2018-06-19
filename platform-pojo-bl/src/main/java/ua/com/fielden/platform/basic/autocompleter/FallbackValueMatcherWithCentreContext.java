package ua.com.fielden.platform.basic.autocompleter;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.orderBy;
import static ua.com.fielden.platform.utils.EntityUtils.hasDescProperty;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
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

    private final Class<T> entityType;

    public FallbackValueMatcherWithCentreContext(final IEntityDao<T> co) {
        super(co);
        entityType = co.getEntityType();
    }

    @Override
    public Integer getPageSize() {
        return 10;
    }

    @Override
    protected ConditionModel makeSearchCriteriaModel(final CentreContext<T, ?> context, final String searchString) {
    	return super.makeSearchCriteriaModel(context, searchString);
    }

    @Override
    protected OrderingModel makeOrderingModel(final String searchString) {
    	if (hasDescProperty(entityType) && !"%".equals(searchString)) {
    		return orderBy().order(createKeyBeforeDescOrderingModel(entityType, searchString)).order(super.makeOrderingModel(searchString)).model();
    	} 
        return super.makeOrderingModel(searchString);
    }
}