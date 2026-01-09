package ua.com.fielden.platform.eql.stage0;

import jakarta.annotation.Nullable;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.IRetrievalModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.antlr.EqlCompilationResult;
import ua.com.fielden.platform.eql.antlr.EqlCompiler;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage1.QueryComponents1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.queries.ResultQuery1;
import ua.com.fielden.platform.eql.stage1.queries.SourceQuery1;
import ua.com.fielden.platform.eql.stage1.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage1.queries.SubQueryForExists1;
import ua.com.fielden.platform.eql.stage1.sources.ISource1;
import ua.com.fielden.platform.eql.stage1.sundries.OrderBys1;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static ua.com.fielden.platform.eql.stage1.conditions.Conditions1.EMPTY_CONDITIONS;

/**
 * Transforms EQL models in the form of fluent API tokens to the stage 1 representation.
 *
 */
public class QueryModelToStage1Transformer {
    public final QueryNowValue nowValue;
    public final IFilter filter;
    private final Optional<String> username;
    private final Map<String, Object> paramValues = new HashMap<>();

    public QueryModelToStage1Transformer(
            final IFilter filter,
            final Optional<String> username,
            final QueryNowValue nowValue,
            final Map<String, Object> paramValues)
    {
        this.filter = filter;
        this.username = username;
        this.nowValue = nowValue;
        this.paramValues.putAll(paramValues);
    }

    public QueryModelToStage1Transformer() {
        this(null, Optional.empty(), null, emptyMap());
    }

    private int sourceId = 0;

    public int nextSourceId() {
        sourceId = sourceId + 1;
        return sourceId;
    }

    public <T extends AbstractEntity<?>, Q extends QueryModel<T>> ResultQuery1 generateAsResultQuery(final QueryModel<T> qm, final OrderingModel orderModel, final IRetrievalModel<T> fetchModel) {
        return new ResultQuery1(parseTokensIntoComponents(qm, orderModel), qm.getResultType(), fetchModel);
    }

    public <T extends AbstractEntity<?>> SourceQuery1 generateAsCorrelatedSourceQuery(final QueryModel<T> qryModel) {
        return generateAsSourceQuery(qryModel, true);
    }

    public <T extends AbstractEntity<?>> SourceQuery1 generateAsUncorrelatedSourceQuery(final QueryModel<T> qryModel) {
        return generateAsSourceQuery(qryModel, false);
    }

    private <T extends AbstractEntity<?>> SourceQuery1 generateAsSourceQuery(final QueryModel<T> qryModel, final boolean isCorrelated) {
        return new SourceQuery1(parseTokensIntoComponents(qryModel, null), qryModel.getResultType(), isCorrelated);
    }

    public SubQuery1 generateAsSubQuery(final QueryModel<?> qryModel) {
        return new SubQuery1(parseTokensIntoComponents(qryModel, null), qryModel.getResultType());
    }

    public SubQueryForExists1 generateAsSubQueryForExists(final QueryModel<?> qryModel) {
        return new SubQueryForExists1(parseTokensIntoComponents(qryModel, null));
    }

    private QueryComponents1 parseTokensIntoComponents(final QueryModel<?> qryModel, final @Nullable OrderingModel orderModel) {
        final EqlCompilationResult.Select result = new EqlCompiler(this).compile(qryModel.getTokenSource(), EqlCompilationResult.Select.class);

        if (orderModel != null && !result.orderBys().isEmpty()) {
            throw new OrderingModelConflictException("Ordering model cannot be specified both as standalone and as part of a query.");
        }
        final OrderBys1 orderBys = orderModel != null ? produceOrderBys(orderModel) : result.orderBys();

        final Conditions1 udfModel = result.maybeJoinRoot()
                .map(joinRoot -> generateUserDataFilteringCondition(qryModel.isFilterable(), filter, username, joinRoot.mainSource()))
                .orElse(EMPTY_CONDITIONS);
        return new QueryComponents1(
                result.maybeJoinRoot(), result.whereConditions(), udfModel, result.yields(), result.groups(),
                orderBys,
                qryModel.isYieldAll(), qryModel.shouldMaterialiseCalcPropsAsColumnsInSqlQuery());
    }

    private Conditions1 generateUserDataFilteringCondition(
            final boolean filterable,
            final IFilter filter,
            final Optional<String> username,
            final ISource1<?> mainSource)
    {
        if (filterable && filter != null) {
            // now there is no need to rely on the main source alias while processing UDF (that's why null can be used until alias parameter is removed from the enhance() method.
            final ConditionModel filteringCondition = filter.enhance(mainSource.sourceType(), null, username.orElse(null));
            if (filteringCondition != null) {
                // LOGGER.debug("\nApplied user-driven-filter to query main source type [" + mainSource.sourceType().getSimpleName() + "]");
                return new EqlCompiler(this).compile(filteringCondition.getTokenSource(), EqlCompilationResult.StandaloneCondition.class).model();
            }
        }

        return EMPTY_CONDITIONS;
    }

    private OrderBys1 produceOrderBys(final OrderingModel orderModel) {
        final EqlCompilationResult.StandaloneOrderBy result = new EqlCompiler(this).compile(orderModel.getTokenSource(), EqlCompilationResult.StandaloneOrderBy.class);
        return result.model();
    }

    public Object getParamValue(final String paramName) {
        return paramValues.get(paramName);
    }


    public boolean hasParam(final String paramName) {
        return paramValues.containsKey(paramName);
    }

}
