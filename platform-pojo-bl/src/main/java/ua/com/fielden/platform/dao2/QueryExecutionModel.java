package ua.com.fielden.platform.dao2;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.QueryModel;

public class QueryExecutionModel<T> {
    private final QueryModel queryModel;
    private final QueryModel orderModel;
    private final fetch fetchModel;
    private final Map<String, Object> paramValues;
    private final boolean lightweight;
    //private final IFilter filter;

    private QueryExecutionModel(final Builder builder) {
	queryModel = builder.queryModel;
	orderModel = builder.orderModel;
	fetchModel = builder.fetchModel;
	paramValues = builder.paramValues;
	lightweight = builder.lightweight;
    }

    public QueryModel getQueryModel() {
        return queryModel;
    }

    public QueryModel getOrderModel() {
        return orderModel;
    }

    public fetch getFetchModel() {
        return fetchModel;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public boolean isLightweight() {
        return lightweight;
    }

    public static class Builder {
	    private QueryModel queryModel;
	    private QueryModel orderModel;
	    private fetch fetchModel;
	    private Map<String, Object> paramValues = new HashMap<String, Object>();
	    private boolean lightweight = false;
	    //private IFilter filter;

	public QueryExecutionModel build() {
	    return new QueryExecutionModel(this);
	}

	public Builder(final QueryModel queryModel) {
	    this.queryModel = queryModel;
	}

	public Builder orderModel(final QueryModel val) {
	    orderModel = val;
	    return this;
	}

	public Builder fetchModel(final fetch val) {
	    fetchModel = val;
	    return this;
	}

	public Builder paramValues(final Map<String, Object> val) {
	    paramValues.putAll(val);
	    return this;
	}

	public Builder paramValue(final String name, final Object value) {
	    paramValues.put(name, value);
	    return this;
	}

	public Builder lightweight(final boolean val) {
	    lightweight = val;
	    return this;
	}
    }
}