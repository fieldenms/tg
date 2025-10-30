package ua.com.fielden.platform.dao;

import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nullable;
import org.apache.logging.log4j.Logger;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.ValuePreprocessor;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.*;
import ua.com.fielden.platform.utils.ToString.IFormat;
import ua.com.fielden.platform.utils.ToString.IFormattable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.unmodifiableMap;
import static org.apache.logging.log4j.LogManager.getLogger;
import static ua.com.fielden.platform.entity.query.model.IFillModel.emptyFillModel;
import static ua.com.fielden.platform.utils.ToString.separateLines;

/**
 * <b>QEM</b> (Query Execution Model) represents a query with additional data required for query execution by the EQL engine.
 *
 * <h3> Entity Instrumentation </h3>
 * Instrumentation of entity instances produced by a QEM can be controlled in the following ways:
 * <ol>
 *   <li> By settings the <i>lightweightedness</i> of a QEM ({@link Builder#lightweight()}).
 *        If a QEM is lightweight, then entity instances should not be instrumented.
 *   <li> Via a fetch model ({@link Builder#with(fetch)}). See {@link fetch#isInstrumented()}.
 *        If a fetch model is instrumented, then entity instances are instrumented.
 * </ol>
 * <b>A fetch model's instrumentation setting has higher precedence than the QEM's lightweightedness</b>.
 * That is, if a QEM is lightweight but its fetch model is instrumented, then the resulting entities will be instrumented.
 * <hr><br>
 *
 * {@linkplain ua.com.fielden.platform.companion.IEntityReader Entity companion methods} that retrieve entities from a
 * data store accept this structure.
 *
 * @param <T>  the type of entities produced by the underlying query
 * @param <Q>  the type of the underlying query
 */
public final class QueryExecutionModel<T extends AbstractEntity<?>, Q extends QueryModel<T>> implements IFormattable {
    private final Q queryModel;
    private final @Nullable OrderingModel orderModel;
    private final @Nullable fetch<T> fetchModel;
    private final IFillModel<T> fillModel;
    /** Immutable map of parameters for the query. */
    private final Map<String, /*@Nullable*/ Object> paramValues;
    private final boolean lightweight;

    private static final ValuePreprocessor valuePreprocessor = new ValuePreprocessor();
    private static final Logger logger = getLogger(QueryExecutionModel.class);

    // Supposedly used for serialisation.
    private QueryExecutionModel() {
        queryModel = null;
        orderModel = null;
        fetchModel = null;
        fillModel = null;
        paramValues = null;
        lightweight = false;
    }

    /**
     * Private constructor that doesn't perform defensive copying.
     * <b>Do not increase its visibility!</b>
     */
    private QueryExecutionModel(
            final Q queryModel,
            final @Nullable OrderingModel orderModel,
            final @Nullable fetch<T> fetchModel,
            final IFillModel<T> fillModel,
            final Map<String, Object> paramValues,
            final boolean lightweight)
    {
        if (fetchModel != null) {
            checkInstrumentation(lightweight, fetchModel, queryModel);
        }
        this.queryModel = queryModel;
        this.orderModel = orderModel;
        this.fetchModel = fetchModel;
        this.fillModel = fillModel;
        this.paramValues = paramValues;
        this.lightweight = lightweight;
    }

    private static void checkInstrumentation(final boolean lightweight, final fetch<?> fetchModel, final QueryModel<?> queryModel) {
        if (lightweight && fetchModel.isInstrumented()) {
            logger.warn("""
            Conflicting instrumentation settings: QEM is lightweight but fetch model is instrumented. \
            Instrumentation will be enabled.
            Entity type: %s
            Query: %s\
            """.formatted(fetchModel.getEntityType().getTypeName(), queryModel));
        }
    }

    private QueryExecutionModel(final Builder<T, Q> builder) {
        this(builder.queryModel, builder.orderModel, builder.fetchModel, builder.fillModel, preprocessParamValues(builder.parameters()), builder.lightweight);
    }

    private static Map<String, Object> preprocessParamValues(final Map<String, Object> paramValues) {
        if (paramValues.isEmpty()) {
            return ImmutableMap.of();
        }
        else {
            final var result = new HashMap<String, Object>(paramValues.size(), 1);
            paramValues.forEach((key, value) -> result.put(key, valuePreprocessor.apply(value)));
            return unmodifiableMap(result);
        }
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

    public IFillModel<T> getFillModel() {
        return fillModel;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
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

    /**
     * Builds a {@link QueryExecutionModel}.
     * <p>
     * This builder is <b>mutable</b> and must not be modified after {@link #model()} has been called.
     */
    public static class Builder<T extends AbstractEntity<?>, Q extends QueryModel<T>> {

        private final Q queryModel;
        private @Nullable OrderingModel orderModel;
        private @Nullable fetch<T> fetchModel;
        private IFillModel<T> fillModel = emptyFillModel();

        /** Don't access directly!
         * @see #addParam(String, Object)
         * @see #addParams(Map)
         * @see #parameters()
         */
        private @Nullable Map<String, Object> paramValues;

        private boolean lightweight = false;

        private Builder(final EntityResultQueryModel<T> queryModel) {
            this.queryModel = (Q) queryModel;
        }

        private Builder(final AggregatedResultQueryModel queryModel) {
            this.queryModel = (Q) queryModel;
        }

        private void addParam(final String name, final Object value) {
            if (paramValues == null) {
                paramValues = new HashMap<>(5);
            }
            paramValues.put(name, value);
        }

        private void addParams(final Map<String, /*@Nullable*/ Object> params) {
            if (paramValues == null) {
                paramValues = new HashMap<>(params);
            }
            else {
                paramValues.putAll(params);
            }
        }

        Map<String, Object> parameters() {
            return paramValues == null ? ImmutableMap.of() : unmodifiableMap(paramValues);
        }

        /**
         * Builds a QEM and returns it.
         * <p>
         * This is a terminal method, and this builder must not be modified after it has been called.
         */
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

        public Builder<T, Q> with(final IFillModel<T> fillModel) {
            this.fillModel = fillModel;
            return this;
        }

        public Builder<T, Q> with(final Map<String, Object> val) {
            addParams(val);
            return this;
        }

        /**
         * Adds a parameter for the query.
         */
        public Builder<T, Q> with(final String name, final @Nullable Object value) {
            addParam(name, value);
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
               obj instanceof QueryExecutionModel<?,?> that
               && lightweight == that.lightweight
               && Objects.equals(orderModel, that.orderModel)
               && Objects.equals(queryModel, that.queryModel)
               && Objects.equals(fetchModel, that.fetchModel)
               && Objects.equals(fillModel, that.fillModel)
               && Objects.equals(paramValues, that.paramValues);
    }


    @Override
    public String toString() {
        return toString(separateLines());
    }

    @Override
    public String toString(final IFormat format) {
        return format.toString(this)
                .add("light", lightweight)
                .addIfNotNull("query", queryModel)
                .addIfNotNull("orderModel", orderModel)
                .addIf("parameters", paramValues, it -> it != null && !it.isEmpty())
                .addIfNotNull("fetch", fetchModel)
                .addIf("fillModel", fillModel, it -> it != null && !it.isEmpty())
                .$();
    }

}
