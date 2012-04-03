package ua.com.fielden.platform.dao2;

import java.util.HashMap;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;

public final class QueryExecutionModel<T extends AbstractEntity<?>> {
    private final QueryModel<T> queryModel;
    private final OrderingModel orderModel;
    private final fetch<T> fetchModel;
    private final Map<String, Object> paramValues;
    private final boolean lightweight;

    private QueryExecutionModel(final Builder<T> builder) {
	queryModel = builder.queryModel;
	orderModel = builder.orderModel;
	fetchModel = builder.fetchModel;
	paramValues = builder.paramValues;
	lightweight = builder.lightweight;
    }

    public QueryModel<T> getQueryModel() {
        return queryModel;
    }

    public OrderingModel getOrderModel() {
        return orderModel;
    }

    public fetch<T> getFetchModel() {
        return fetchModel;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public boolean isLightweight() {
        return lightweight;
    }

    public static <E extends AbstractEntity<?>> Builder<E> from(final EntityResultQueryModel<E> queryModel) {
	return new Builder<E>(queryModel);
    }

    public static Builder<EntityAggregates> from(final AggregatedResultQueryModel queryModel) {
	return new Builder<EntityAggregates>(queryModel);
    }

    public static class Builder<T extends AbstractEntity<?>> {
	    private QueryModel<T> queryModel;
	    private OrderingModel orderModel;
	    private fetch<T> fetchModel;
	    private Map<String, Object> paramValues = new HashMap<String, Object>();
	    private boolean lightweight = false;

	public QueryExecutionModel<T> build() {
	    return new QueryExecutionModel<T>(this);
	}

	private Builder(final EntityResultQueryModel<T> queryModel) {
	    this.queryModel = queryModel;
	}

	private Builder(final AggregatedResultQueryModel queryModel) {
	    this.queryModel = (QueryModel<T>) queryModel;
	}

	public Builder<T> with(final OrderingModel val) {
	    orderModel = val;
	    return this;
	}

	public Builder<T> with(final fetch<T> val) {
	    fetchModel = val;
	    return this;
	}

	public Builder<T> with(final Map<String, Object> val) {
	    paramValues.putAll(val);
	    return this;
	}

	public Builder<T> with(final String name, final Object value) {
	    paramValues.put(name, value);
	    return this;
	}

	public Builder<T> lightweight(final boolean val) {
	    lightweight = val;
	    return this;
	}
    }
}