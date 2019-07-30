package ua.com.fielden.platform.entity.query.fluent;

import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.ADD;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.DIV;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.MOD;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.MULT;
import static ua.com.fielden.platform.entity.query.fluent.enums.ArithmeticalOperator.SUB;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.EQ;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.GE;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.GT;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.LE;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.LT;
import static ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator.NE;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.DAY;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.HOUR;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.MINUTE;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.MONTH;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.SECOND;
import static ua.com.fielden.platform.entity.query.fluent.enums.DateIntervalUnit.YEAR;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.ABS;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.ADD_DATE_INTERVAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.AVERAGE;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.AVERAGE_DISTINCT;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.CASE_WHEN;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.CONCAT;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.COUNT;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.COUNT_ALL;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.COUNT_DATE_INTERVAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.COUNT_DISTINCT;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.DATE;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.IF_NULL;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.LOWERCASE;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.MAX;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.MIN;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.NOW;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.ROUND;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.SUM;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.SUM_DISTINCT;
import static ua.com.fielden.platform.entity.query.fluent.enums.Functions.UPPERCASE;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.IJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.JoinType.LJ;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.OR;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.ASC;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.DESC;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.FROM;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.GROUP_BY;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.ON;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.ORDER_BY;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.WHERE;
import static ua.com.fielden.platform.entity.query.fluent.enums.QueryTokens.YIELD;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ARITHMETICAL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.AS_ALIAS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.AS_ALIAS_REQUIRED;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.BEGIN_COND;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.BEGIN_EXPR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.COLLECTIONAL_FUNCTION;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.COMPARISON_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.COND_START;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.COND_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.CRIT_COND_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.END_COND;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.END_EXPR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.END_FUNCTION;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ENTITY_TYPE_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXISTS_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXT_PROP;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.FUNCTION;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.FUNCTION_INTERVAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IN_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IPARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IVAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.JOIN_TYPE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.LIKE_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.LOGICAL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.NEGATED_COND_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.NULL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ORDER_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PROP;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_MODELS_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QRY_SOURCE_ALIAS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.QUERY_TOKEN;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SORT_ORDER;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.VAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.VALUES_AS_QRY_SOURCE;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ZERO_ARG_FUNCTION;
import static ua.com.fielden.platform.utils.EntityUtils.equalsEx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.enums.Functions;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

/**
 * Contains internal structure (incrementally collected building blocks) of the entity query request.
 * 
 * @author TG Team
 * 
 */
final class Tokens {
    private final List<Pair<TokenCategory, Object>> values = new ArrayList<>();
    private Class<? extends AbstractEntity<?>> mainSourceType;
    private boolean yieldAll;
    private final ValuePreprocessor valuePreprocessor;

    public Tokens() {
        valuePreprocessor = new ValuePreprocessor();
    }

    private Tokens(final ValuePreprocessor valuePreprocessor, final Class<? extends AbstractEntity<?>> mainSourceType, final boolean yieldAll) {
        this.valuePreprocessor = valuePreprocessor;
        this.mainSourceType = mainSourceType;
        this.yieldAll = yieldAll;
    }

    @Override
    public String toString() {
        return values.toString();
    }

    private Tokens cloneTokens() {
        final Tokens result = new Tokens(valuePreprocessor, mainSourceType, yieldAll);
        result.values.addAll(values);
        return result;
    }
    
    private Tokens add(final TokenCategory cat, final Object value) {
        final Tokens result = cloneTokens();
        result.values.add(new Pair<TokenCategory, Object>(cat, value));
        return result;
    }

    private Tokens add(final TokenCategory cat1, final Object value1, final TokenCategory cat2, final Object value2) {
        final Tokens result = cloneTokens();
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
        return add(LOGICAL_OPERATOR, AND);
    }

    public Tokens or() {
        return add(LOGICAL_OPERATOR, OR);
    }

    public Tokens beginCondition(final boolean negated) {
        return add(BEGIN_COND, negated);
    }

    public Tokens endCondition() {
        return add(END_COND, null);
    }

    public Tokens beginExpression() {
        return add(BEGIN_EXPR, null);
    }

    public Tokens endExpression() {
        return add(END_EXPR, null);
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

    public Tokens critCondition(final String propName, final String critPropName) {
        return add(CRIT_COND_OPERATOR, new Pair<String, String>(propName, critPropName));
    }

    public Tokens critCondition(final ICompoundCondition0<?> collectionQueryStart, final String propName, final String critPropName) {
        return add(CRIT_COND_OPERATOR, new Pair<T2<ICompoundCondition0<?>, String>, String>(t2(collectionQueryStart, propName), critPropName));
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

    public Tokens like(final LikeOptions options) {
        return add(LIKE_OPERATOR, options);
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

    public Tokens addDateInterval() {
        return add(FUNCTION, ADD_DATE_INTERVAL);
    }

    public Tokens countDateIntervalFunction() {
        return add(FUNCTION, COUNT_DATE_INTERVAL);
    }

    public Tokens secondsInterval() {
        return add(FUNCTION_INTERVAL, SECOND);
    }

    public Tokens minutesInterval() {
        return add(FUNCTION_INTERVAL, MINUTE);
    }

    public Tokens hoursInterval() {
        return add(FUNCTION_INTERVAL, HOUR);
    }

    public Tokens daysInterval() {
        return add(FUNCTION_INTERVAL, DAY);
    }

    public Tokens monthsInterval() {
        return add(FUNCTION_INTERVAL, MONTH);
    }

    public Tokens yearsInterval() {
        return add(FUNCTION_INTERVAL, YEAR);
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
        return add(FUNCTION, Functions.SECOND);
    }

    public Tokens minuteOf() {
        return add(FUNCTION, Functions.MINUTE);
    }

    public Tokens hourOf() {
        return add(FUNCTION, Functions.HOUR);
    }

    public Tokens dayOf() {
        return add(FUNCTION, Functions.DAY);
    }

    public Tokens monthOf() {
        return add(FUNCTION, Functions.MONTH);
    }

    public Tokens yearOf() {
        return add(FUNCTION, Functions.YEAR);
    }

    public Tokens dayOfWeekOf() {
        return add(FUNCTION, Functions.DAY_OF_WEEK);
    }

    public Tokens dateOf() {
        return add(FUNCTION, DATE);
    }

    public Tokens absOf() {
        return add(FUNCTION, ABS);
    }

    public Tokens sumOf() {
        return add(COLLECTIONAL_FUNCTION, SUM);
    }

    public Tokens maxOf() {
        return add(COLLECTIONAL_FUNCTION, MAX);
    }

    public Tokens minOf() {
        return add(COLLECTIONAL_FUNCTION, MIN);
    }

    public Tokens countOf() {
        return add(COLLECTIONAL_FUNCTION, COUNT);
    }

    public Tokens averageOf() {
        return add(COLLECTIONAL_FUNCTION, AVERAGE);
    }

    public Tokens sumOfDistinct() {
        return add(COLLECTIONAL_FUNCTION, SUM_DISTINCT);
    }

    public Tokens countOfDistinct() {
        return add(COLLECTIONAL_FUNCTION, COUNT_DISTINCT);
    }

    public Tokens averageOfDistinct() {
        return add(COLLECTIONAL_FUNCTION, AVERAGE_DISTINCT);
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

    public Tokens order(OrderingModel order) {
        return add(ORDER_TOKENS, order);
    }
    
    public Tokens asc() {
        return add(SORT_ORDER, ASC);
    }

    public Tokens desc() {
        return add(SORT_ORDER, DESC);
    }

    public Tokens on() {
        return add(TokenCategory.ON, ON);
    }

    public Tokens conditionStart() {
        return add(COND_START, null);
    }

    public Tokens endOfFunction() {
        return add(END_FUNCTION, null);
    }

    public Tokens endOfFunction(final ITypeCast typeCast) {
        return add(END_FUNCTION, typeCast);
    }

    public Tokens where() {
        return add(QUERY_TOKEN, WHERE);
    }

    public Tokens yield() {
        return add(QUERY_TOKEN, YIELD);
    }
    
    public Tokens yieldAll() {
        final Tokens result = cloneTokens();
        result.yieldAll = true;
        return result;
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

    public <E extends AbstractEntity<?>> Tokens from() {
        this.mainSourceType = EntityAggregates.class;
        return add(QUERY_TOKEN, FROM, VALUES_AS_QRY_SOURCE, EntityAggregates.class);
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
            return add(QUERY_TOKEN, FROM, QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public <T extends AbstractEntity<?>> Tokens from(final EntityResultQueryModel<T>... sourceModels) {
        if (sourceModels.length >= 1) {
            this.mainSourceType = sourceModels[0].getResultType();
            return add(QUERY_TOKEN, FROM, QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
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
            return add(JOIN_TYPE, IJ, QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public <E extends AbstractEntity<?>> Tokens innerJoin(final EntityResultQueryModel<E>... sourceModels) {
        if (sourceModels.length >= 1) {
            return add(JOIN_TYPE, IJ, QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public Tokens leftJoin(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length >= 1) {
            return add(JOIN_TYPE, LJ, QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
        } else {
            throw new RuntimeException("No models have been specified as a source in from statement!");
        }
    }

    public <E extends AbstractEntity<?>> Tokens leftJoin(final EntityResultQueryModel<E>... sourceModels) {
        if (sourceModels.length >= 1) {
            return add(JOIN_TYPE, LJ, QRY_MODELS_AS_QRY_SOURCE, Arrays.asList(sourceModels));
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

    public boolean isYieldAll() {
        return yieldAll;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((mainSourceType == null) ? 0 : mainSourceType.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		result = prime * result + (yieldAll ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		if (!(obj instanceof Tokens)) {
		    return false;
		}
		
		final Tokens that = (Tokens) obj;
		
		return equalsEx(this.mainSourceType, that.mainSourceType) &&
		       equalsEx(this.yieldAll, that.yieldAll) &&
		       equalsEx(this.values, that.values);
	}
}