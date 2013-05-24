package ua.com.fielden.platform.report.query.generation;

import java.util.Map;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

/**
 * Provides contract that allows one to get components of the query execution model.
 *
 * @author TG Team
 *
 * @param <T>
 */
public interface IQueryComposer<T extends AbstractEntity<?>> {

    /**
     * Returns the condition query.
     *
     * @return
     */
    ICompleted<T> getQuery();

    /**
     * Returns the fetch model of the query.
     *
     * @return
     */
    fetch<T> getFetch();

    /**
     * Returns ordering model of the query.
     *
     * @return
     */
    OrderingModel getOrdering();

    /**
     * Returns the parameter's map of the query.
     *
     * @return
     */
    Map<String, Object> getParams();

    /**
     * Composes the query execution model from the components defined in the
     * {@link #getQuery()}, {@link #getFetch()}, {@link #getOrdering()} and {@link #getParams()} methods.
     *
     * @return
     */
    QueryExecutionModel<T, EntityResultQueryModel<T>> composeQuery();
}
