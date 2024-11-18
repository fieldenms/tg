package ua.com.fielden.platform.dao;

import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.ValuePreprocessor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.model.IFillModel.EMPTY_FILL_MODEL;

public final class QueryExecutionModel<T extends AbstractEntity<?>, Q extends QueryModel<T>> {
    private final Q queryModel;
    private final OrderingModel orderModel;
    private final fetch<T> fetchModel;
    private final IFillModel fillModel;
    private final Map<String, Object> paramValues;
    private final boolean lightweight;
    private final ValuePreprocessor valuePreprocessor = new ValuePreprocessor();
    private static final Logger logger = getLogger(QueryExecutionModel.class);

    protected QueryExecutionModel() {
        queryModel = null;
        orderModel = null;
        fetchModel = null;
        fillModel = null;
        paramValues = null;
        lightweight = false;
    }
    
    protected QueryExecutionModel(final Q queryModel, final OrderingModel orderModel, final fetch<T> fetchModel, final IFillModel fillModel, final Map<String, Object> paramValues, final boolean lightweight) {
        this.queryModel = queryModel;
        this.orderModel = orderModel;
        this.fetchModel = fetchModel;
        this.fillModel = fillModel;
        this.paramValues = new HashMap<>();
        this.paramValues.putAll(paramValues);
        this.lightweight = lightweight;
    }

    private QueryExecutionModel(final Builder<T, Q> builder) {
        queryModel = builder.queryModel;
        orderModel = builder.orderModel;
        fetchModel = builder.fetchModel;
        fillModel = builder.fillModel;
        paramValues = preprocessParamValues(builder.paramValues);
        lightweight = builder.lightweight;
        logger.debug(this);
    }

    /**
     * A convenient copy method.
     * 
     * @return
     */
    public QueryExecutionModel<T, Q> copy() {
        return new QueryExecutionModel<>(this.queryModel, this.orderModel, this.fetchModel, this.fillModel, this.paramValues, this.lightweight);
    }

    @Override
    public String toString() {
        final var sb = new StringBuilder(64);
        sb.append("QEM {\n");
        if (fetchModel != null) { sb.append("  fetch: "); sb.append(fetchModel); sb.append('\n'); }
        if (fillModel != null && !fillModel.isEmpty()) { sb.append("  fill: "); sb.append(fillModel); sb.append('\n'); }
        if (queryModel != null) { sb.append("  query: "); sb.append(queryModel); sb.append('\n'); }
        if (orderModel != null) { sb.append("  order: "); sb.append(orderModel); sb.append('\n'); }
        if (paramValues != null && !paramValues.isEmpty()) { sb.append("  params: "); sb.append(paramValues); sb.append('\n'); }
        sb.append("  light: "); sb.append(lightweight);
        sb.append("\n}");
        return sb.toString();
    }

    private Map<String, Object> preprocessParamValues(final Map<String, Object> paramValues) {
        final Map<String, Object> result = new HashMap<>();
        for (final Map.Entry<String, Object> entry : paramValues.entrySet()) {
            result.put(entry.getKey(), valuePreprocessor.apply(entry.getValue()));
        }
        return result;
    }

    public Q getQueryModel() {
        return queryModel;
    }

    public OrderingModel getOrderModel() {
        return orderModel;
    }

    public fetch<T> getFetchModel() {
        return fetchModel;
    }

    public IFillModel getFillModel() {
        return fillModel;
    }

    public Map<String, Object> getParamValues() {
        return Collections.unmodifiableMap(paramValues);
    }

    public boolean isLightweight() {
        return lightweight;
    }

    public static <E extends AbstractEntity<?>> Builder<E, EntityResultQueryModel<E>> from(final EntityResultQueryModel<E> queryModel) {
        return new Builder<>(queryModel);
    }

    public static Builder<EntityAggregates, AggregatedResultQueryModel> from(final AggregatedResultQueryModel queryModel) {
        return new Builder<>(queryModel);
    }

    public QueryExecutionModel<T, Q> lightweight() {
        return new QueryExecutionModel<>(this.queryModel, this.orderModel, this.fetchModel, this.fillModel, this.paramValues, true);
    }
    
    public static class Builder<T extends AbstractEntity<?>, Q extends QueryModel<T>> {
        private Q queryModel;
        private OrderingModel orderModel;
        private fetch<T> fetchModel;
        private IFillModel fillModel = EMPTY_FILL_MODEL;
        private Map<String, Object> paramValues = new HashMap<>();
        private boolean lightweight = false;

        private Builder(final EntityResultQueryModel<T> queryModel) {
            this.queryModel = (Q) queryModel;
        }

        private Builder(final AggregatedResultQueryModel queryModel) {
            this.queryModel = (Q) queryModel;
        }

        public QueryExecutionModel<T, Q> model() {
            return new QueryExecutionModel<>(this);
        }

        public Builder<T, Q> with(final OrderingModel val) {
            orderModel = val;
            return this;
        }

        public Builder<T, Q> with(final fetch<T> val) {
            fetchModel = val;
            return this;
        }

        public Builder<T, Q> with(final IFillModel fillModel) {
            this.fillModel = fillModel;
            return this;
        }

        public Builder<T, Q> with(final Map<String, Object> val) {
            paramValues.putAll(val);
            return this;
        }

        public Builder<T, Q> with(final String name, final Object value) {
            paramValues.put(name, value);
            return this;
        }

        public Builder<T, Q> lightweight() {
            lightweight = true;
            return this;
        }

        public Builder<T, Q> lightweight(final boolean value) {
            lightweight = value;
            return this;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(fetchModel, fillModel, lightweight, orderModel, paramValues, queryModel);
    }

    @Override
    public boolean equals(final Object obj) {
        return this == obj ||
               obj instanceof QueryExecutionModel that
               && lightweight == that.lightweight
               && Objects.equals(orderModel, that.orderModel)
               && Objects.equals(queryModel, that.queryModel)
               && Objects.equals(fetchModel, that.fetchModel)
               && Objects.equals(fillModel, that.fillModel)
               && Objects.equals(paramValues, that.paramValues);
    }
}
