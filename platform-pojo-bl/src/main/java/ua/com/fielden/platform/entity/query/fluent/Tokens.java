package ua.com.fielden.platform.entity.query.fluent;

import java.util.ArrayList;
import java.util.Arrays;
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

    private Tokens(final ValuePreprocessor valuePreprocessor, final Class<? extends AbstractEntity<?>>  mainSourceType) {
	this.valuePreprocessor = valuePreprocessor;
	this.mainSourceType = mainSourceType;
    }

    @Override
    public String toString() {
	return values.toString();
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
	    result.addAll(Arrays.asList(items));
	}
	return result;
    }

    public Tokens and() {
	return add(TokenCategory.LOGICAL_OPERATOR, LogicalOperator.AND);
    }

    public Tokens or() {
	return add(TokenCategory.LOGICAL_OPERATOR, LogicalOperator.OR);
    }

    public Tokens beginCondition(final boolean negated) {
	return add(TokenCategory.BEGIN_COND, negated);
    }

    public Tokens endCondition() {
	return add(TokenCategory.END_COND, null);
    }

    public Tokens beginExpression() {
	return add(TokenCategory.BEGIN_EXPR, null);
    }

    public Tokens endExpression() {
	return add(TokenCategory.END_EXPR, null);
    }

    public Tokens exists(final boolean negated, final QueryModel model) {
	return add(TokenCategory.EXISTS_OPERATOR, negated, TokenCategory.EQUERY_TOKENS, model);
    }

    public Tokens existsAnyOf(final boolean negated, final QueryModel... subQueries) {
	return add(TokenCategory.EXISTS_OPERATOR, negated, TokenCategory.ANY_OF_EQUERY_TOKENS, getListFromArray(subQueries));
    }

    public Tokens existsAllOf(final boolean negated, final QueryModel... subQueries) {
	return add(TokenCategory.EXISTS_OPERATOR, negated, TokenCategory.ALL_OF_EQUERY_TOKENS, getListFromArray(subQueries));
    }

    public Tokens isNull(final boolean negated) {
	return add(TokenCategory.NULL_OPERATOR, negated);
    }

    public Tokens lt() {
	return add(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.LT);
    }

    public Tokens gt() {
	return add(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.GT);
    }

    public Tokens le() {
	return add(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.LE);
    }

    public Tokens ge() {
	return add(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.GE);
    }

    public Tokens eq() {
	return add(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.EQ);
    }

    public Tokens ne() {
	return add(TokenCategory.COMPARISON_OPERATOR, ComparisonOperator.NE);
    }

    public Tokens like(final boolean negated) {
	return add(TokenCategory.LIKE_OPERATOR, negated);
    }

    public Tokens iLike(final boolean negated) {
	return add(TokenCategory.ILIKE_OPERATOR, negated);
    }

    public Tokens in(final boolean negated) {
	return add(TokenCategory.IN_OPERATOR, negated);
    }

    public Tokens yield(final String yieldName) {
	return add(TokenCategory.YIELD, yieldName);
    }

    public Tokens prop(final String propName) {
	return add(TokenCategory.PROP, propName);
    }

    public Tokens extProp(final String propName) {
	return add(TokenCategory.EXT_PROP, propName);
    }

    public Tokens val(final Object value) {
	return add(TokenCategory.VAL, valuePreprocessor.apply(value));
    }

    public Tokens iVal(final Object value) {
	return add(TokenCategory.IVAL, valuePreprocessor.apply(value));
    }

    public Tokens model(final PrimitiveResultQueryModel model) {
	return add(TokenCategory.EQUERY_TOKENS, model);
    }

    public Tokens model(final SingleResultQueryModel model) {
	return add(TokenCategory.EQUERY_TOKENS, model);
    }

    public Tokens param(final String paramName) {
	if (!StringUtils.isEmpty(paramName)) {
	    return add(TokenCategory.PARAM, paramName);
	} else {
	    throw new RuntimeException("Param name has not been specified");
	}
    }

    public Tokens iParam(final String paramName) {
	if (!StringUtils.isEmpty(paramName)) {
	    return add(TokenCategory.IPARAM, paramName);
	} else {
	    throw new RuntimeException("Param name has not been specified");
	}
    }

    public Tokens expr(final ExpressionModel exprModel) {
	return add(TokenCategory.EXPR_TOKENS, exprModel);
    }

    public Tokens cond(final ConditionModel conditionModel) {
	return add(TokenCategory.COND_TOKENS, conditionModel);
    }

    public Tokens as(final String yieldAlias) {
	return add(TokenCategory.AS_ALIAS, yieldAlias);
    }

    public Tokens anyOfProps(final String... props) {
	return add(TokenCategory.ANY_OF_PROPS, getListFromArray(props));
    }

    public Tokens anyOfParams(final String... params) {
	return add(TokenCategory.ANY_OF_PARAMS, getListFromArray(params));
    }

    public Tokens anyOfIParams(final String... params) {
	return add(TokenCategory.ANY_OF_IPARAMS, getListFromArray(params));
    }

    public Tokens anyOfModels(final PrimitiveResultQueryModel... models) {
	return add(TokenCategory.ANY_OF_EQUERY_TOKENS, getListFromArray(models));
    }

    public Tokens anyOfValues(final Object... values) {
	return add(TokenCategory.ANY_OF_VALUES, valuePreprocessor.apply(values));
    }

    public Tokens anyOfExpressions(final ExpressionModel... expressions) {
	return add(TokenCategory.ANY_OF_EXPR_TOKENS, getListFromArray(expressions));
    }

    public Tokens allOfProps(final String... props) {
	return add(TokenCategory.ALL_OF_PROPS, getListFromArray(props));
    }

    public Tokens allOfParams(final String... params) {
	return add(TokenCategory.ALL_OF_PARAMS, getListFromArray(params));
    }

    public Tokens allOfIParams(final String... params) {
	return add(TokenCategory.ALL_OF_IPARAMS, getListFromArray(params));
    }

    public Tokens allOfModels(final PrimitiveResultQueryModel... models) {
	return add(TokenCategory.ALL_OF_EQUERY_TOKENS, getListFromArray(models));
    }

    public Tokens allOfValues(final Object... values) {
	return add(TokenCategory.ALL_OF_VALUES, valuePreprocessor.apply(values));
    }

    public Tokens allOfExpressions(final ExpressionModel... expressions) {
	return add(TokenCategory.ALL_OF_EXPR_TOKENS, getListFromArray(expressions));
    }

    public Tokens any(final SingleResultQueryModel subQuery) {
	return add(TokenCategory.ANY_OPERATOR, subQuery);
    }

    public Tokens all(final SingleResultQueryModel subQuery) {
	return add(TokenCategory.ALL_OPERATOR, subQuery);
    }

    public Tokens setOfProps(final String... props) {
	return add(TokenCategory.SET_OF_PROPS, getListFromArray(props));
    }

    public Tokens setOfParams(final String... params) {
	return add(TokenCategory.SET_OF_PARAMS, getListFromArray(params));
    }

    public Tokens setOfIParams(final String... params) {
	return add(TokenCategory.SET_OF_IPARAMS, getListFromArray(params));
    }

    public Tokens setOfValues(final Object... values) {
	return add(TokenCategory.SET_OF_VALUES, valuePreprocessor.apply(values));
    }

    public Tokens setOfExpressions(final ExpressionModel... expressions) {
	return add(TokenCategory.SET_OF_EXPR_TOKENS, getListFromArray(expressions));
    }

    public Tokens countDateIntervalFunction() {
	return add(TokenCategory.FUNCTION, Functions.COUNT_DATE_INTERVAL);
    }

    public Tokens secondsInterval() {
	return add(TokenCategory.FUNCTION_INTERVAL, DateIntervalUnit.SECOND);
    }

    public Tokens minutesInterval() {
	return add(TokenCategory.FUNCTION_INTERVAL, DateIntervalUnit.MINUTE);
    }

    public Tokens hoursInterval() {
	return add(TokenCategory.FUNCTION_INTERVAL, DateIntervalUnit.HOUR);
    }

    public Tokens daysInterval() {
	return add(TokenCategory.FUNCTION_INTERVAL, DateIntervalUnit.DAY);
    }

    public Tokens monthsInterval() {
	return add(TokenCategory.FUNCTION_INTERVAL, DateIntervalUnit.MONTH);
    }

    public Tokens yearsInterval() {
	return add(TokenCategory.FUNCTION_INTERVAL, DateIntervalUnit.YEAR);
    }

    public Tokens caseWhenFunction() {
	return add(TokenCategory.FUNCTION, Functions.CASE_WHEN);
    }

    public Tokens concat() {
	return add(TokenCategory.FUNCTION, Functions.CONCAT);
    }

    public Tokens round() {
	return add(TokenCategory.FUNCTION, Functions.ROUND);
    }

    public Tokens to(final Integer precision) {
	return add(TokenCategory.VAL, precision);
    }

    public Tokens ifNull() {
	return add(TokenCategory.FUNCTION, Functions.IF_NULL);
    }

    public Tokens uppercase() {
	return add(TokenCategory.FUNCTION, Functions.UPPERCASE);
    }

    public Tokens lowercase() {
	return add(TokenCategory.FUNCTION, Functions.LOWERCASE);
    }

    public Tokens now() {
	return add(TokenCategory.ZERO_ARG_FUNCTION, Functions.NOW);
    }

    public Tokens secondOf() {
	return add(TokenCategory.FUNCTION, Functions.SECOND);
    }

    public Tokens minuteOf() {
	return add(TokenCategory.FUNCTION, Functions.MINUTE);
    }

    public Tokens hourOf() {
	return add(TokenCategory.FUNCTION, Functions.HOUR);
    }

    public Tokens dayOf() {
	return add(TokenCategory.FUNCTION, Functions.DAY);
    }

    public Tokens monthOf() {
	return add(TokenCategory.FUNCTION, Functions.MONTH);
    }

    public Tokens yearOf() {
	return add(TokenCategory.FUNCTION, Functions.YEAR);
    }

    public Tokens sumOf() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.SUM);
    }

    public Tokens maxOf() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.MAX);
    }

    public Tokens minOf() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.MIN);
    }

    public Tokens countOf() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.COUNT);
    }

    public Tokens averageOf() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.AVERAGE);
    }

    public Tokens sumOfDistinct() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.SUM_DISTINCT);
    }

    public Tokens countOfDistinct() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.COUNT_DISTINCT);
    }

    public Tokens averageOfDistinct() {
	return add(TokenCategory.COLLECTIONAL_FUNCTION, Functions.AVERAGE_DISTINCT);
    }

    public Tokens countAll() {
	return add(TokenCategory.ZERO_ARG_FUNCTION, Functions.COUNT_ALL);
    }

    public Tokens add() {
	return add(TokenCategory.ARITHMETICAL_OPERATOR, ArithmeticalOperator.ADD);
    }

    public Tokens subtract() {
	return add(TokenCategory.ARITHMETICAL_OPERATOR, ArithmeticalOperator.SUB);
    }

    public Tokens divide() {
	return add(TokenCategory.ARITHMETICAL_OPERATOR, ArithmeticalOperator.DIV);
    }

    public Tokens multiply() {
	return add(TokenCategory.ARITHMETICAL_OPERATOR, ArithmeticalOperator.MULT);
    }

    public Tokens asc() {
	return add(TokenCategory.SORT_ORDER, QueryTokens.ASC);
    }

    public Tokens desc() {
	return add(TokenCategory.SORT_ORDER, QueryTokens.DESC);
    }

    public Tokens on() {
	return add(TokenCategory.ON, QueryTokens.ON);
    }

    public Tokens conditionStart() {
	return add(TokenCategory.COND_START, null);
    }

    public Tokens endOfFunction() {
	return add(TokenCategory.END_FUNCTION, null);
    }

    public Tokens where() {
	return add(TokenCategory.QUERY_TOKEN, QueryTokens.WHERE);
    }

    public Tokens yield() {
	return add(TokenCategory.QUERY_TOKEN, QueryTokens.YIELD);
    }

    public Tokens groupBy() {
	return add(TokenCategory.QUERY_TOKEN, QueryTokens.GROUP_BY);
    }

    public Tokens orderBy() {
	return add(TokenCategory.QUERY_TOKEN, QueryTokens.ORDER_BY);
    }

    public Tokens joinAlias(final String alias) {
	return add(TokenCategory.QRY_SOURCE_ALIAS, alias);
    }

    public <E extends AbstractEntity<?>> Tokens from(final Class<E> entityType) {
	if (entityType == null) {
	    throw new IllegalArgumentException("Missing entity type in query: " + this.values);
	}
	this.mainSourceType = entityType;
	return add(TokenCategory.QUERY_TOKEN, QueryTokens.FROM, TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE, entityType);
    }

    public Tokens from(final AggregatedResultQueryModel... sourceModels) {
	if (sourceModels.length >= 1) {
	    this.mainSourceType = EntityAggregates.class;
	    return add(TokenCategory.QUERY_TOKEN, QueryTokens.FROM, TokenCategory.QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
	} else {
	    throw new RuntimeException("No models have been specified as a source in from statement!");
	}
    }

    public <T extends AbstractEntity<?>> Tokens from(final EntityResultQueryModel<T>... sourceModels) {
	if (sourceModels.length >= 1) {
	    this.mainSourceType = sourceModels[0].getResultType();
	    return add(TokenCategory.QUERY_TOKEN, QueryTokens.FROM, TokenCategory.QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
	} else {
	    throw new RuntimeException("No models have been specified as a source in from statement!");
	}
    }

    public <E extends AbstractEntity<?>> Tokens innerJoin(final Class<E> entityType) {
	return add(TokenCategory.JOIN_TYPE, JoinType.IJ, TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE, entityType);
    }

    public <E extends AbstractEntity<?>> Tokens leftJoin(final Class<E> entityType) {
	return add(TokenCategory.JOIN_TYPE, JoinType.LJ, TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE, entityType);
    }

    public Tokens innerJoin(final AggregatedResultQueryModel... sourceModels) {
	if (sourceModels.length >= 1) {
	    return add(TokenCategory.JOIN_TYPE, JoinType.IJ, TokenCategory.QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
	} else {
	    throw new RuntimeException("No models have been specified as a source in from statement!");
	}
    }

    public <E extends AbstractEntity<?>> Tokens innerJoin(final EntityResultQueryModel<E>... sourceModels) {
	if (sourceModels.length >= 1) {
	    return add(TokenCategory.JOIN_TYPE, JoinType.IJ, TokenCategory.QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
	} else {
	    throw new RuntimeException("No models have been specified as a source in from statement!");
	}
    }

    public Tokens leftJoin(final AggregatedResultQueryModel... sourceModels) {
	if (sourceModels.length >= 1) {
	    return add(TokenCategory.JOIN_TYPE, JoinType.LJ, TokenCategory.QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
	} else {
	    throw new RuntimeException("No models have been specified as a source in from statement!");
	}
    }

    public <E extends AbstractEntity<?>> Tokens leftJoin(final EntityResultQueryModel<E>... sourceModels) {
	if (sourceModels.length >= 1) {
	    return add(TokenCategory.JOIN_TYPE, JoinType.LJ, TokenCategory.QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
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