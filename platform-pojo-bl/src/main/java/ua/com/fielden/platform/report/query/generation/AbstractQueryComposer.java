package ua.com.fielden.platform.report.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * The abstract implementation of the {@link IQueryComposer} interface that implements only {@link #composeQuery()} method.
 *
 * @author TG Team
 *
 * @param <T>
 */
public abstract class AbstractQueryComposer<T extends AbstractEntity<?>> implements IQueryComposer<T> {

    @Override
    public final QueryExecutionModel<T, EntityResultQueryModel<T>> composeQuery() {
	if (getQuery() == null) {
	    throw new IllegalStateException("The query is not defined!");
	}
	Builder<T, EntityResultQueryModel<T>> queryBuilder = from(getQuery().model());
	queryBuilder = getFetch() == null ? queryBuilder : queryBuilder.with(getFetch());
	queryBuilder = getOrdering() == null ? queryBuilder : queryBuilder.with(getOrdering());
	queryBuilder = getParams() == null ? queryBuilder : queryBuilder.with(getParams());
	return queryBuilder.model();
    }

}
