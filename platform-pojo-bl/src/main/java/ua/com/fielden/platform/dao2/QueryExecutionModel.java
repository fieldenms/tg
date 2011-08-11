package ua.com.fielden.platform.dao2;

import java.util.Map;

import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.QueryModel;

public class QueryExecutionModel<T> {
    private final QueryModel queryModel;
    private final QueryModel orderModel;
    private final fetch fetchModel;
    private final Map<String, Object> paramValues;

    public QueryExecutionModel(final QueryModel queryModel, final QueryModel orderModel, final fetch fetchModel, final Map<String, Object> paramValues) {
	super();
	this.queryModel = queryModel;
	this.orderModel = orderModel;
	this.fetchModel = fetchModel;
	this.paramValues = paramValues;
    }

}
