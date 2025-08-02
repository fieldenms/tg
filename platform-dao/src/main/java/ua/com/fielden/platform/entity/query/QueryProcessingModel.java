package ua.com.fielden.platform.entity.query;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.utils.ToString.IFormat;
import ua.com.fielden.platform.utils.ToString.IFormattable;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;
import static ua.com.fielden.platform.utils.ToString.separateLines;

public final class QueryProcessingModel<T extends AbstractEntity<?>, Q extends QueryModel<T>> implements IFormattable {
    public final Q queryModel;
    public final @Nullable OrderingModel orderModel;
    public final @Nullable IRetrievalModel<T> fetchModel;
    private final Map<String, Object> paramValues;
    public final boolean lightweight;

    public QueryProcessingModel(
            final Q queryModel,
            final @Nullable OrderingModel orderModel,
            final @Nullable IRetrievalModel<T> fetchModel,
            final Map<String, Object> paramValues,
            final boolean lightweight)
    {
        this.queryModel = queryModel;
        this.orderModel = orderModel;
        this.fetchModel = fetchModel;
        this.paramValues = new HashMap<>();
        this.paramValues.putAll(paramValues);
        this.lightweight = lightweight;
    }
    
    public  Map<String, Object> getParamValues() {
        return unmodifiableMap(paramValues);
    }

    @Override
    public String toString() {
        return toString(separateLines());
    }

    @Override
    public String toString(final IFormat format) {
        return format.toString(this)
                .add("light", lightweight)
                .add("query", queryModel)
                .addIfNotNull("orderModel", orderModel)
                .addIfNotNull("fetch", fetchModel)
                .addIfNotEmpty("parameters", paramValues)
                .$();
    }

}
