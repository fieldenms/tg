package ua.com.fielden.platform.dao2;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

public class AggregatesQueryExecutionModel {
    private final AggregatedResultQueryModel queryModel;
    private final OrderingModel orderModel;
    private final fetch<EntityAggregates> fetchModel;
    private final Map<String, Object> paramValues;
    private final boolean lightweight;

    private AggregatesQueryExecutionModel(final Builder builder) {
	queryModel = builder.queryModel;
	orderModel = builder.orderModel;
	fetchModel = builder.fetchModel;
	paramValues = builder.paramValues;
	lightweight = builder.lightweight;
    }

    public AggregatedResultQueryModel getQueryModel() {
        return queryModel;
    }

    public OrderingModel getOrderModel() {
        return orderModel;
    }

    public fetch<EntityAggregates> getFetchModel() {
        return fetchModel;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public boolean isLightweight() {
        return lightweight;
    }

    public static class Builder {
	    private AggregatedResultQueryModel queryModel;
	    private OrderingModel orderModel;
	    private fetch<EntityAggregates> fetchModel;
	    private Map<String, Object> paramValues = new HashMap<String, Object>();
	    private boolean lightweight = false;

	public AggregatesQueryExecutionModel build() {
	    return new AggregatesQueryExecutionModel(this);
	}

	public Builder(final AggregatedResultQueryModel queryModel) {
	    this.queryModel = queryModel;
	}

	public Builder orderModel(final OrderingModel val) {
	    orderModel = val;
	    return this;
	}

	public Builder fetchModel(final fetch<EntityAggregates> val) {
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