package ua.com.fielden.platform.entity.query.fluent;

import java.util.ArrayList;
import static java.util.Arrays.*;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.*;
import static ua.com.fielden.platform.entity.query.fluent.ComparisonOperator.*;
import static ua.com.fielden.platform.entity.query.fluent.LogicalOperator.*;
import static ua.com.fielden.platform.entity.query.fluent.Functions.*;
import static ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator.*;
import static ua.com.fielden.platform.entity.query.fluent.QuerySection.*;
import static ua.com.fielden.platform.entity.query.fluent.JoinType.*;
import static ua.com.fielden.platform.entity.query.fluent.SortingOrderDirection.*;
/**
 * Contains internal structure (incrementally collected building blocks) of the entity query request.
 * 
 * @author TG Team
 * 
 */
final class Tokens {
    private final List<Pair<TokenCategory, Object>> values = new ArrayList<Pair<TokenCategory, Object>>();
    private Class<? extends AbstractEntity<?>> mainSourceType;
    private final ValuePreprocessor valuePreprocessor;

    public Tokens() {
        valuePreprocessor = new ValuePreprocessor();
    }

    private Tokens(final ValuePreprocessor valuePreprocessor, final Class<? extends AbstractEntity<?>> mainSourceType) {
        this.valuePreprocessor = valuePreprocessor;
        this.mainSourceType = mainSourceType;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    private Tokens add(final TokenCategory cat) {
        return add(cat, null);
    }

    private Tokens add(final TokenCategory cat, final Object value) {
        final Tokens result = new Tokens(valuePreprocessor, mainSourceType);
        result.values.addAll(values);
        result.values.add(new Pair<TokenCategory, Object>(cat, value));
        return result;
    }

    private Tokens add(final TokenCategory cat1, final Object value1, final TokenCategory cat2, final Object value2) {
        final Tokens result = new Tokens(valuePreprocessor, mainSourceType);
        result.values.addAll(values);
        result.values.add(new Pair<TokenCategory, Object>(cat1, value1));
        result.values.add(new Pair<TokenCategory, Object>(cat2, value2));
        return result;
    }

    private <E extends Object> List<E> getListFromArray(final E... items) {
        final List<E> result = new ArrayList<E>();
        if (items != null) {
            result.addAll(asList(items));
        }
        return result;
    }

    public Tokens and() {
        return add(LOGICAL_OPERATOR, AND);
    }

    public Tokens or() {
        return add(LOGICAL_OPERATOR, OR);
    }

    public Tokens beginCondition(final boolean negated) {
        return add(BEGIN_COND, negated);
    }

    public Tokens endCondition() {
        return add(END_COND);
    }

    public Tokens beginExpression() {
        return add(BEGIN_EXPR);
    }

    public Tokens endExpression() {
        return add(END_EXPR);
    }

    public Tokens exists(final boolean negated, final QueryModel model) {
        return add(EXISTS_OPERATOR, negated, EQUERY_TOKENS, model);
    }

    public Tokens existsAnyOf(final boolean negated, final QueryModel... subQueries) {
        return add(EXISTS_OPERATOR, negated, ANY_OF_EQUERY_TOKENS, getListFromArray(subQueries));
    }

    public Tokens existsAllOf(final boolean negated, final QueryModel... subQueries) {
        return add(EXISTS_OPERATOR, negated, ALL_OF_EQUERY_TOKENS, getListFromArray(subQueries));
    }

    public Tokens isNull(final boolean negated) {
        return add(NULL_OPERATOR, negated);
    }

    public Tokens lt() {
        return add(COMPARISON_OPERATOR, LT);
    }

    public Tokens gt() {
        return add(COMPARISON_OPERATOR, GT);
    }

    public Tokens le() {
        return add(COMPARISON_OPERATOR, LE);
    }

    public Tokens ge() {
        return add(COMPARISON_OPERATOR, GE);
    }

    public Tokens eq() {
        return add(COMPARISON_OPERATOR, EQ);
    }

    public Tokens ne() {
        return add(COMPARISON_OPERATOR, NE);
    }

    public Tokens like(final boolean negated) {
        return add(LIKE_OPERATOR, negated);
    }

    public Tokens iLike(final boolean negated) {
        return add(ILIKE_OPERATOR, negated);
    }

    public Tokens in(final boolean negated) {
        return add(IN_OPERATOR, negated);
    }

    public Tokens yield(final String yieldName) {
        return add(TokenCategory.YIELD, yieldName);
    }

    public Tokens prop(final String propName) {
        return add(PROP, propName);
    }

    public Tokens extProp(final String propName) {
        return add(EXT_PROP, propName);
    }

    public Tokens val(final Object value) {
        // TODO need YieldNull to allow this exception
        //	if (value == null) {
        //	    throw new RuntimeException("Shouldn't pass Null to val(...) method. Use iVal(...) if condition autoignore feature is required.");
        //	}
        return add(VAL, valuePreprocessor.apply(value));
    }

    public Tokens iVal(final Object value) {
        return add(IVAL, valuePreprocessor.apply(value));
    }

    public Tokens model(final PrimitiveResultQueryModel model) {
        return add(EQUERY_TOKENS, model);
    }

    public Tokens model(final SingleResultQueryModel model) {
        return add(EQUERY_TOKENS, model);
    }

    public Tokens param(final String paramName) {
        if (!StringUtils.isEmpty(paramName)) {
            return add(PARAM, paramName);
        } else {
            throw new RuntimeException("Param name has not been specified");
        }
    }

    public Tokens iParam(final String paramName) {
        if (!StringUtils.isEmpty(paramName)) {
            return add(IPARAM, paramName);
        } else {
            throw new RuntimeException("Param name has not been specified");
        }
    }

    public Tokens expr(final ExpressionModel exprModel) {
        return add(EXPR_TOKENS, exprModel);
    }

    public Tokens cond(final ConditionModel conditionModel) {
        return add(COND_TOKENS, conditionModel);
    }

    public Tokens negatedCond(final ConditionModel conditionModel) {
        return add(NEGATED_COND_TOKENS, conditionModel);
    }

    public Tokens as(final String yieldAlias) {
        return add(AS_ALIAS, yieldAlias);
    }

    public Tokens asRequired(final String yieldAlias) {
        return add(AS_ALIAS_REQUIRED, yieldAlias);
    }

    public Tokens anyOfProps(final String... props) {
        return add(ANY_OF_PROPS, getListFromArray(props));
    }

    public Tokens anyOfParams(final String... params) {
        return add(ANY_OF_PARAMS, getListFromArray(params));
    }

    public Tokens anyOfIParams(final String... params) {
        return add(ANY_OF_IPARAMS, getListFromArray(params));
    }

    public Tokens anyOfModels(final PrimitiveResultQueryModel... models) {
        return add(ANY_OF_EQUERY_TOKENS, getListFromArray(models));
    }

    public Tokens anyOfValues(final Object... values) {
        return add(ANY_OF_VALUES, valuePreprocessor.apply(values));
    }

    public Tokens anyOfExpressions(final ExpressionModel... expressions) {
        return add(ANY_OF_EXPR_TOKENS, getListFromArray(expressions));
    }

    public Tokens allOfProps(final String... props) {
        return add(ALL_OF_PROPS, getListFromArray(props));
    }

    public Tokens allOfParams(final String... params) {
        return add(ALL_OF_PARAMS, getListFromArray(params));
    }

    public Tokens allOfIParams(final String... params) {
        return add(ALL_OF_IPARAMS, getListFromArray(params));
    }

    public Tokens allOfModels(final PrimitiveResultQueryModel... models) {
        return add(ALL_OF_EQUERY_TOKENS, getListFromArray(models));
    }

    public Tokens allOfValues(final Object... values) {
        return add(ALL_OF_VALUES, valuePreprocessor.apply(values));
    }

    public Tokens allOfExpressions(final ExpressionModel... expressions) {
        return add(ALL_OF_EXPR_TOKENS, getListFromArray(expressions));
    }

    public Tokens any(final SingleResultQueryModel subQuery) {
        return add(ANY_OPERATOR, subQuery);
    }

    public Tokens all(final SingleResultQueryModel subQuery) {
        return add(ALL_OPERATOR, subQuery);
    }

    public Tokens setOfProps(final String... props) {
        return add(SET_OF_PROPS, getListFromArray(props));
    }

    public Tokens setOfParams(final String... params) {
        return add(SET_OF_PARAMS, getListFromArray(params));
    }

    public Tokens setOfIParams(final String... params) {
        return add(SET_OF_IPARAMS, getListFromArray(params));
    }

    public Tokens setOfValues(final Object... values) {
        return add(SET_OF_VALUES, valuePreprocessor.apply(values));
    }

    public Tokens setOfExpressions(final ExpressionModel... expressions) {
        return add(SET_OF_EXPR_TOKENS, getListFromArray(expressions));
    }

    public Tokens countDateIntervalFunction() {
        return add(FUNCTION, COUNT_DATE_INTERVAL);
    }

    public Tokens secondsInterval() {
        return add(FUNCTION_INTERVAL, DateIntervalUnit.SECOND);
    }

    public Tokens minutesInterval() {
        return add(FUNCTION_INTERVAL, DateIntervalUnit.MINUTE);
    }

    public Tokens hoursInterval() {
        return add(FUNCTION_INTERVAL, DateIntervalUnit.HOUR);
    }

    public Tokens daysInterval() {
        return add(FUNCTION_INTERVAL, DateIntervalUnit.DAY);
    }

    public Tokens monthsInterval() {
        return add(FUNCTION_INTERVAL, DateIntervalUnit.MONTH);
    }

    public Tokens yearsInterval() {
        return add(FUNCTION_INTERVAL, DateIntervalUnit.YEAR);
    }

    public Tokens caseWhenFunction() {
        return add(FUNCTION, CASE_WHEN);
    }

    public Tokens concat() {
        return add(FUNCTION, CONCAT);
    }

    public Tokens round() {
        return add(FUNCTION, ROUND);
    }

    public Tokens to(final Integer precision) {
        return add(VAL, precision);
    }

    public Tokens ifNull() {
        return add(FUNCTION, IF_NULL);
    }

    public Tokens uppercase() {
        return add(FUNCTION, UPPERCASE);
    }

    public Tokens lowercase() {
        return add(FUNCTION, LOWERCASE);
    }

    public Tokens now() {
        return add(ZERO_ARG_FUNCTION, NOW);
    }

    public Tokens secondOf() {
        return add(FUNCTION, SECOND);
    }

    public Tokens minuteOf() {
        return add(FUNCTION, MINUTE);
    }

    public Tokens hourOf() {
        return add(FUNCTION, HOUR);
    }

    public Tokens dayOf() {
        return add(FUNCTION, DAY);
    }

    public Tokens monthOf() {
        return add(FUNCTION, MONTH);
    }

    public Tokens yearOf() {
        return add(FUNCTION, YEAR);
    }

    public Tokens dateOf() {
        return add(FUNCTION, DATE);
    }

    public Tokens absOf() {
        return add(FUNCTION, ABS);
    }

    public Tokens sumOf() {
        return add(AGGREGATE_FUNCTION, SUM);
    }

    public Tokens maxOf() {
        return add(AGGREGATE_FUNCTION, MAX);
    }

    public Tokens minOf() {
        return add(AGGREGATE_FUNCTION, MIN);
    }

    public Tokens countOf() {
        return add(AGGREGATE_FUNCTION, COUNT);
    }

    public Tokens averageOf() {
        return add(AGGREGATE_FUNCTION, AVERAGE);
    }

    public Tokens sumOfDistinct() {
        return add(AGGREGATE_FUNCTION, SUM_DISTINCT);
    }

    public Tokens countOfDistinct() {
        return add(AGGREGATE_FUNCTION, COUNT_DISTINCT);
    }

    public Tokens averageOfDistinct() {
        return add(AGGREGATE_FUNCTION, AVERAGE_DISTINCT);
    }

    public Tokens countAll() {
        return add(ZERO_ARG_FUNCTION, COUNT_ALL);
    }

    public Tokens add() {
        return add(ARITHMETICAL_OPERATOR, ADD);
    }

    public Tokens subtract() {
        return add(ARITHMETICAL_OPERATOR, SUB);
    }

    public Tokens divide() {
        return add(ARITHMETICAL_OPERATOR, DIV);
    }

    public Tokens multiply() {
        return add(ARITHMETICAL_OPERATOR, MULT);
    }

    public Tokens modulo() {
        return add(ARITHMETICAL_OPERATOR, MOD);
    }

    public Tokens asc() {
        return add(SORT_ORDER, ASC);
    }

    public Tokens desc() {
        return add(SORT_ORDER, DESC);
    }

    public Tokens on() {
        return add(ON);
    }

    public Tokens conditionStart() {
        return add(COND_START);
    }

    public Tokens endOfFunction() {
        return add(END_FUNCTION);
    }

    public Tokens endOfFunction(final ITypeCast typeCast) {
        return add(END_FUNCTION, typeCast);
    }

    public Tokens where() {
        return add(QUERY_TOKEN, WHERE);
    }

    public Tokens yield() {
        return add(QUERY_TOKEN, SELECT);
    }

    public Tokens groupBy() {
        return add(QUERY_TOKEN, GROUP_BY);
    }

    public Tokens orderBy() {
        return add(QUERY_TOKEN, ORDER_BY);
    }

    public Tokens joinAlias(final String alias) {
        return add(QRY_SOURCE_ALIAS, alias);
    }

    public <E extends AbstractEntity<?>> Tokens from(final Class<E> entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("Missing entity type in query: " + this.values);
        }
        this.mainSourceType = entityType;
        return add(QUERY_TOKEN, FROM, ENTITY_TYPE_AS_QRY_SOURCE, entityType);
    }

    public Tokens from(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length >= 1) {
            this.mainSourceType = EntityAggregates.class;
            return add(QUERY_TOKEN, FROM, QRY_MODELS_AS_QRY_SOURCE, asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public <T extends AbstractEntity<?>> Tokens from(final EntityResultQueryModel<T>... sourceModels) {
        if (sourceModels.length >= 1) {
            this.mainSourceType = sourceModels[0].getResultType();
            return add(QUERY_TOKEN, FROM, QRY_MODELS_AS_QRY_SOURCE, asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public <E extends AbstractEntity<?>> Tokens innerJoin(final Class<E> entityType) {
        return add(JOIN_TYPE, IJ, ENTITY_TYPE_AS_QRY_SOURCE, entityType);
    }

    public <E extends AbstractEntity<?>> Tokens leftJoin(final Class<E> entityType) {
        return add(JOIN_TYPE, LJ, ENTITY_TYPE_AS_QRY_SOURCE, entityType);
    }

    public Tokens innerJoin(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length >= 1) {
            return add(JOIN_TYPE, IJ, QRY_MODELS_AS_QRY_SOURCE, asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public <E extends AbstractEntity<?>> Tokens innerJoin(final EntityResultQueryModel<E>... sourceModels) {
        if (sourceModels.length >= 1) {
            return add(JOIN_TYPE, IJ, QRY_MODELS_AS_QRY_SOURCE, asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public Tokens leftJoin(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length >= 1) {
            return add(JOIN_TYPE, LJ, QRY_MODELS_AS_QRY_SOURCE, asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public <E extends AbstractEntity<?>> Tokens leftJoin(final EntityResultQueryModel<E>... sourceModels) {
        if (sourceModels.length >= 1) {
            return add(JOIN_TYPE, LJ, QRY_MODELS_AS_QRY_SOURCE, asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public List<Pair<TokenCategory, Object>> getValues() {
        return values;
    }

    public Class<? extends AbstractEntity<?>> getMainSourceType() {
        return mainSourceType;
    }
}