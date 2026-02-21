package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.dao.QueryExecutionModel.Builder;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.*;
import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

/// Provides convenient entry points to start building EQL statements and fetch models.
///
public class EntityQueryUtils {

    //:::::::::::::::::::::::::::: SELECT / FROM :::::::::::::::::::::::::::::::

    /// Starts building a query that selects from the specified entity type.
    ///
    public static <T extends AbstractEntity<?>> IFromAlias<T> select(final Class<T> entityType) {
        return new FromAlias<>(new EqlSentenceBuilder().from(entityType));
    }

    /// Starts building a query that selects from one or more entity query models.
    ///
    @SafeVarargs
    public static <T extends AbstractEntity<?>> IFromAlias<T> select(final EntityResultQueryModel<T>... sourceQueryModels) {
        return new FromAlias<>(new EqlSentenceBuilder().from(sourceQueryModels));
    }

    /// Starts building a query that selects from the specified entity query model.
    ///
    public static <T extends AbstractEntity<?>> IFromAlias<T> select(final EntityResultQueryModel<T> sourceQueryModel) {
        return new FromAlias<>(new EqlSentenceBuilder().from(sourceQueryModel));
    }

    /// Starts building a query that selects from the specified aggregated result query model.
    ///
    public static IFromAlias<EntityAggregates> select(final AggregatedResultQueryModel sourceQueryModel) {
        return new FromAlias<>(new EqlSentenceBuilder().from(new AggregatedResultQueryModel[] { sourceQueryModel }));
    }

    /// Starts building a query that selects from one or more aggregated result query models.
    ///
    public static IFromAlias<EntityAggregates> select(final AggregatedResultQueryModel... sourceQueryModels) {
        return new FromAlias<>(new EqlSentenceBuilder().from(sourceQueryModels));
    }

    /// Starts building a query that has no `FROM` clause (e.g. for scalar expressions).
    ///
    public static <T extends AbstractEntity<?>> IFromNone<T> select() {
        return new FromNone<>(new EqlSentenceBuilder().from());
    }

    //:::::::::::::::::::::::::::: EXPR / COND :::::::::::::::::::::::::::::::::

    /// Starts building a standalone EQL expression.
    ///
    public static IStandAloneExprOperand expr() {
        return new StandAloneExpOperand(new EqlSentenceBuilder().expr());
    }

    /// Starts building a standalone EQL condition.
    public static <ET extends AbstractEntity<?>> IStandAloneConditionOperand<ET> cond() {
        return new StandAloneConditionOperand<>(new EqlSentenceBuilder().cond());
    }

    //:::::::::::::::::::::::::::: ORDER BY ::::::::::::::::::::::::::::::::::::

    /// Starts building a standalone `ORDER BY` clause.
    ///
    public static StandaloneOrderBy.IOrderingItem orderBy() {
        return new StandaloneOrderBy_OrderingItem(new EqlSentenceBuilder().orderBy());
    }

    //:::::::::::::::::::::::::::: QUERY EXECUTION :::::::::::::::::::::::::::::

    /// Creates a query execution model builder for the given entity result query model.
    ///
    public static <T extends AbstractEntity<?>> Builder<T, EntityResultQueryModel<T>>
    from(final EntityResultQueryModel<T> queryModel) {
        return QueryExecutionModel.<T> from(queryModel);
    }

    /// Creates a query execution model builder for the given aggregated result query model.
    ///
    public static Builder<EntityAggregates, AggregatedResultQueryModel>
    from(final AggregatedResultQueryModel queryModel) {
        return QueryExecutionModel.from(queryModel);
    }

    //:::::::::::::::::::::::::::: FETCH MODELS ::::::::::::::::::::::::::::::::

    /// Creates a fetch model for loading only IDs of the specified entity type.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchIdOnly(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.ID_ONLY);
    }

    /// Creates a default fetch model for the specified entity type.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetch(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.DEFAULT);
    }

    /// Creates a default fetch model for the specified entity type with instrumentation enabled.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchAndInstrument(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.DEFAULT, true);
    }

    /// Creates a fetch model that loads all properties of the specified entity type.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchAll(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.ALL);
    }

    /// Creates a fetch model that loads all properties of the specified entity type with instrumentation enabled.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchAllAndInstrument(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.ALL, true);
    }

    /// Creates a fetch model that loads all properties, including calculated ones.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchAllInclCalc(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.ALL_INCL_CALC);
    }

    /// Creates a fetch model that loads all properties, including calculated ones, with instrumentation enabled.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchAllInclCalcAndInstrument(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.ALL_INCL_CALC, true);
    }

    /// Creates a fetch model that loads only ID and version for the specified entity type.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchOnly(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.ID_AND_VERSION);
    }

    /// Creates a fetch model that loads only ID and version, with instrumentation enabled.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchOnlyAndInstrument(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.ID_AND_VERSION, true);
    }

    /// Creates a fetch model that does not load any properties for the specified entity type.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchNone(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.NONE);
    }

    /// Creates a fetch model that does not load any properties, with instrumentation enabled.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchNoneAndInstrument(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.NONE, true);
    }

    /// Creates a fetch model that loads only key and description (implies fetching of ID and version) for the specified entity type.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchKeyAndDescOnly(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.KEY_AND_DESC);
    }

    /// Creates a fetch model that loads only key and description (implies fetching of ID and version), with instrumentation enabled.
    ///
    public static <T extends AbstractEntity<?>> fetch<T> fetchKeyAndDescOnlyAndInstrument(final Class<T> entityType) {
        return new fetch<>(entityType, FetchCategory.KEY_AND_DESC, true);
    }

    /// Creates an empty fetch model for aggregated results.
    /// Specific properties that need to be fetched should be added explicitly.
    ///
    public static fetch<EntityAggregates> fetchAggregates() {
        return new fetch<>(EntityAggregates.class, FetchCategory.NONE);
    }

    //:::::::::::::::::::::::::::: MISC HELPERS ::::::::::::::::::::::::::::::::

    /// Generates an empty condition model for EQL models.
    ///
    /// This condition is a convenience placeholder and is ignored during condition processing.
    ///
    public static ConditionModel emptyCondition() {
        return cond().iVal(null).eq().iVal(null).model();
    }

    /// Extracts exact parameter values from optional wrappers.
    ///
    /// Returns a map from property names to the underlying parameter values,
    /// including only entries where a value is present.
    ///
    /// @param params a map of parameter names to optional values
    /// @return a map of parameter names to unwrapped values
    ///
    public static Map<String, Object> extractExactParams(final Map<String, Optional<?>> params) {
        return params.entrySet().stream().filter(entry -> entry.getValue().isPresent()).collect(toMap(Map.Entry::getKey, entry -> entry.getValue().orElse(null)));
    }

}
