package ua.com.fielden.platform.entity.query;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;

public final class QueryProcessingModel<T extends AbstractEntity<?>, Q extends QueryModel<T>> {
    public final Q queryModel;
    public final OrderingModel orderModel;
    public final fetch<T> fetchModel;
    public final Map<String, Object> paramValues;
    public final boolean lightweight;

    public QueryProcessingModel(final Q queryModel, final OrderingModel orderModel, final fetch<T> fetchModel, final Map<String, Object> paramValues, final boolean lightweight) {
        this.queryModel = queryModel;
        this.orderModel = orderModel;
        this.fetchModel = fetchModel;
        this.paramValues = new HashMap<>();
        this.paramValues.putAll(paramValues);
        this.lightweight = lightweight;
    }
}