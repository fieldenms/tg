package ua.com.fielden.platform.entity.query.fluent;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.*;
import ua.com.fielden.platform.eql.antlr.ListTokenSource;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.util.*;

import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.*;
import static ua.com.fielden.platform.eql.antlr.tokens.SimpleTokens.token;

/**
 * Builds a sentence in the EQL language out of {@linkplain Token ANTLR tokens}.
 *
 * @author TG Team
 */
final class EqlSentenceBuilder {
    private final List<Token> tokens = new ArrayList<>();
    private Class<? extends AbstractEntity<?>> mainSourceType;
    private boolean yieldAll;
    private final ValuePreprocessor valuePreprocessor;

    public EqlSentenceBuilder() {
        valuePreprocessor = new ValuePreprocessor();
    }

    private EqlSentenceBuilder(final ValuePreprocessor valuePreprocessor, final Class<? extends AbstractEntity<?>> mainSourceType, final boolean yieldAll) {
        this.valuePreprocessor = valuePreprocessor;
        this.mainSourceType = mainSourceType;
        this.yieldAll = yieldAll;
    }

    @Override
    public String toString() {
        return tokens.toString();
    }

    private EqlSentenceBuilder makeCopy() {
        final EqlSentenceBuilder copy = new EqlSentenceBuilder(valuePreprocessor, mainSourceType, yieldAll);
        copy.tokens.addAll(tokens);
        return copy;
    }

    private EqlSentenceBuilder _add(final Token token) {
        final EqlSentenceBuilder copy = makeCopy();
        copy.tokens.add(token);
        return copy;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // BUILDING METHODS

    public EqlSentenceBuilder and() {
        return _add(token(AND));
    }

    public EqlSentenceBuilder or() {
        return _add(token(OR));
    }

    public EqlSentenceBuilder beginCondition(final boolean negated) {
        return _add(token(negated ? NOTBEGIN : BEGIN));
    }

    public EqlSentenceBuilder endCondition() {
        return _add(token(END));
    }

    public EqlSentenceBuilder beginExpression() {
        return _add(token(BEGINEXPR));
    }

    public EqlSentenceBuilder endExpression() {
        return _add(token(ENDEXPR));
    }

    public EqlSentenceBuilder exists(final boolean negated, final QueryModel model) {
        return _add(negated ? new NotExistsToken(model) : new ExistsToken(model));
    }

    public EqlSentenceBuilder existsAnyOf(final boolean negated, final QueryModel... subQueries) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder existsAllOf(final boolean negated, final QueryModel... subQueries) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder critCondition(final String propName, final String critPropName) {
        return _add(new CritConditionToken(propName, critPropName));
    }

    public EqlSentenceBuilder critCondition(
            final ICompoundCondition0<?> collectionQueryStart, final String propName, final String critPropName,
            final Optional<Object> defaultValue)
    {
        return _add(new CritConditionToken(collectionQueryStart, propName, critPropName, defaultValue));
    }

    public EqlSentenceBuilder isNull(final boolean negated) {
        return _add(token(negated ? ISNOTNULL : ISNULL));
    }

    public EqlSentenceBuilder lt() {
        return _add(token(LT));
    }

    public EqlSentenceBuilder gt() {
        return _add(token(GT));
    }

    public EqlSentenceBuilder le() {
        return _add(token(LE));
    }

    public EqlSentenceBuilder ge() {
        return _add(token(GE));
    }

    public EqlSentenceBuilder eq() {
        return _add(token(EQ));
    }

    public EqlSentenceBuilder ne() {
        return _add(token(NE));
    }

    public EqlSentenceBuilder like(final LikeOptions options) {
        // TODO options
        return _add(token(LIKE));
    }

    public EqlSentenceBuilder in(final boolean negated) {
        return _add(negated ? token(NOTIN) : token(IN));
    }

    public EqlSentenceBuilder yield(final String yieldName) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder prop(final String propName) {
        return _add(new PropToken(propName));
    }

    public EqlSentenceBuilder extProp(final String propName) {
        return _add(new ExtPropToken(propName));
    }

    public EqlSentenceBuilder val(final Object value) {
        return _add(new ValToken(valuePreprocessor.apply(value)));
    }

    public EqlSentenceBuilder iVal(final Object value) {
        return _add(new IValToken(valuePreprocessor.apply(value)));
    }

    public EqlSentenceBuilder model(final PrimitiveResultQueryModel model) {
        return _add(new QueryModelToken<>(model));
    }

    public EqlSentenceBuilder model(final SingleResultQueryModel model) {
        return _add(new QueryModelToken<>(model));
    }

    public EqlSentenceBuilder param(final String paramName) {
        return _add(new ParamToken(paramName));
    }

    public EqlSentenceBuilder iParam(final String paramName) {
        return _add(new IParamToken(paramName));
    }

    public EqlSentenceBuilder expr() {
        return _add(token(EXPR));
    }

    public EqlSentenceBuilder expr(final ExpressionModel exprModel) {
        return _add(new ExprToken(exprModel));
    }

    public EqlSentenceBuilder cond() {
        return _add(token(COND));
    }

    public EqlSentenceBuilder cond(final ConditionModel conditionModel) {
        return _add(new ConditionToken(conditionModel));
    }

    public EqlSentenceBuilder negatedCond(final ConditionModel conditionModel) {
        return _add(new NegatedConditionToken(conditionModel));
    }

    public EqlSentenceBuilder as(final String yieldAlias) {
        return _add(new AsToken(yieldAlias));
    }

    public EqlSentenceBuilder asRequired(final String yieldAlias) {
        return _add(new AsRequiredToken(yieldAlias));
    }

    public EqlSentenceBuilder anyOfProps(final String... props) {
        return _add(new AnyOfPropsToken(Arrays.asList(props)));
    }

    public EqlSentenceBuilder anyOfProps(final IConvertableToPath... props) {
        return _add(new AnyOfPropsToken(Arrays.stream(props).map(IConvertableToPath::toPath).toList()));
    }

    public EqlSentenceBuilder anyOfParams(final String... params) {
        return _add(new AnyOfParamsToken(Arrays.asList(params)));
    }

    public EqlSentenceBuilder anyOfIParams(final String... params) {
        return _add(new AnyOfIParamsToken(Arrays.asList(params)));
    }

    public EqlSentenceBuilder anyOfModels(final PrimitiveResultQueryModel... models) {
        return _add(new AnyOfModelsToken(Arrays.asList(models)));
    }

    public EqlSentenceBuilder anyOfValues(final Object... values) {
        return _add(new AnyOfValuesToken(valuePreprocessor.applyMany(values).toList()));
    }

    public EqlSentenceBuilder anyOfExpressions(final ExpressionModel... expressions) {
        return _add(new AnyOfExpressionsToken(Arrays.asList(expressions)));
    }

    public EqlSentenceBuilder allOfProps(final String... props) {
        return _add(new AllOfPropsToken(Arrays.asList(props)));
    }

    public EqlSentenceBuilder allOfProps(final IConvertableToPath... props) {
        return _add(new AllOfPropsToken(Arrays.stream(props).map(IConvertableToPath::toPath).toList()));
    }

    public EqlSentenceBuilder allOfParams(final String... params) {
        return _add(new AllOfParamsToken(Arrays.asList(params)));
    }

    public EqlSentenceBuilder allOfIParams(final String... params) {
        return _add(new AllOfIParamsToken(Arrays.asList(params)));
    }

    public EqlSentenceBuilder allOfModels(final PrimitiveResultQueryModel... models) {
        return _add(new AllOfModelsToken(Arrays.asList(models)));
    }

    public EqlSentenceBuilder allOfValues(final Object... values) {
        return _add(new AllOfValuesToken(valuePreprocessor.applyMany(values).toList()));
    }

    public EqlSentenceBuilder allOfExpressions(final ExpressionModel... expressions) {
        return _add(new AllOfExpressionsToken(Arrays.asList(expressions)));
    }

    public EqlSentenceBuilder any(final SingleResultQueryModel subQuery) {
        return _add(new AnyToken(subQuery));
    }

    public EqlSentenceBuilder all(final SingleResultQueryModel subQuery) {
        return _add(new AllToken(subQuery));
    }

    public EqlSentenceBuilder setOfProps(final String... props) {
        return _add(new PropsToken(Arrays.asList(props)));
    }

    public EqlSentenceBuilder setOfProps(final IConvertableToPath... props) {
        return _add(new PropsToken(Arrays.stream(props).map(IConvertableToPath::toPath).toList()));
    }

    public EqlSentenceBuilder setOfParams(final String... params) {
        return _add(new ParamsToken(Arrays.asList(params)));
    }

    public EqlSentenceBuilder setOfIParams(final String... params) {
        return _add(new IParamsToken(Arrays.asList(params)));
    }

    public EqlSentenceBuilder setOfValues(final Object... values) {
        return _add(new ValuesToken(valuePreprocessor.applyMany(values).toList()));
    }

    // TODO remove?
    public EqlSentenceBuilder setOfExpressions(final ExpressionModel... expressions) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder addDateInterval() {
        return _add(token(ADDTIMEINTERVALOF));
    }

    public EqlSentenceBuilder countDateIntervalFunction() {
        return _add(token(COUNT));
    }

    public EqlSentenceBuilder secondsInterval() {
        return _add(token(SECONDS));
    }

    public EqlSentenceBuilder minutesInterval() {
        return _add(token(MINUTES));
    }

    public EqlSentenceBuilder hoursInterval() {
        return _add(token(HOURS));
    }

    public EqlSentenceBuilder daysInterval() {
        return _add(token(DAYS));
    }

    public EqlSentenceBuilder monthsInterval() {
        return _add(token(MONTHS));
    }

    public EqlSentenceBuilder yearsInterval() {
        return _add(token(YEARS));
    }

    public EqlSentenceBuilder between() {
        return _add(token(BETWEEN));
    }

    public EqlSentenceBuilder caseWhenFunction() {
        return _add(token(CASEWHEN));
    }

    public EqlSentenceBuilder then() {
        return _add(token(THEN));
    }

    public EqlSentenceBuilder otherwise() {
        return _add(token(OTHERWISE));
    }

    public EqlSentenceBuilder concat() {
        return _add(token(CONCAT));
    }

    public EqlSentenceBuilder with() {
        return _add(token(WITH));
    }

    public EqlSentenceBuilder round() {
        return _add(token(ROUND));
    }

    public EqlSentenceBuilder to(final int precision) {
        return _add(new ToToken(precision));
    }

    public EqlSentenceBuilder to() {
        return _add(token(TO));
    }

    public EqlSentenceBuilder ifNull() {
        return _add(token(IFNULL));
    }

    public EqlSentenceBuilder uppercase() {
        return _add(token(UPPERCASE));
    }

    public EqlSentenceBuilder lowercase() {
        return _add(token(LOWERCASE));
    }

    public EqlSentenceBuilder now() {
        return _add(token(NOW));
    }

    public EqlSentenceBuilder secondOf() {
        return _add(token(SECONDOF));
    }

    public EqlSentenceBuilder minuteOf() {
        return _add(token(MINUTEOF));
    }

    public EqlSentenceBuilder hourOf() {
        return _add(token(HOUROF));
    }

    public EqlSentenceBuilder dayOf() {
        return _add(token(DAYOF));
    }

    public EqlSentenceBuilder monthOf() {
        return _add(token(MONTHOF));
    }

    public EqlSentenceBuilder yearOf() {
        return _add(token(YEAROF));
    }

    public EqlSentenceBuilder dayOfWeekOf() {
        return _add(token(DAYOFWEEKOF));
    }

    public EqlSentenceBuilder dateOf() {
        return _add(token(DATEOF));
    }

    public EqlSentenceBuilder absOf() {
        return _add(token(ABSOF));
    }

    public EqlSentenceBuilder sumOf() {
        return _add(token(SUMOF));
    }

    public EqlSentenceBuilder maxOf() {
        return _add(token(MAXOF));
    }

    public EqlSentenceBuilder minOf() {
        return _add(token(MINOF));
    }

    public EqlSentenceBuilder countOf() {
        return _add(token(COUNTOF));
    }

    public EqlSentenceBuilder averageOf() {
        return _add(token(AVGOF));
    }

    public EqlSentenceBuilder sumOfDistinct() {
        return _add(token(SUMOFDISTINCT));
    }

    public EqlSentenceBuilder countOfDistinct() {
        return _add(token(COUNTOFDISTINCT));
    }

    public EqlSentenceBuilder averageOfDistinct() {
        return _add(token(AVGOFDISTINCT));
    }

    public EqlSentenceBuilder countAll() {
        return _add(token(COUNTALL));
    }

    public EqlSentenceBuilder add() {
        return _add(token(ADD));
    }

    public EqlSentenceBuilder subtract() {
        return _add(token(SUB));
    }

    public EqlSentenceBuilder divide() {
        return _add(token(DIV));
    }

    public EqlSentenceBuilder multiply() {
        return _add(token(MULT));
    }

    public EqlSentenceBuilder modulo() {
        return _add(token(MOD));
    }

    public EqlSentenceBuilder order(OrderingModel order) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder asc() {
        return _add(token(ASC));
    }

    public EqlSentenceBuilder desc() {
        return _add(token(DESC));
    }

    public EqlSentenceBuilder on() {
        return _add(token(ON));
    }

    public EqlSentenceBuilder conditionStart() {
        return _add(token(WHEN));
    }

    public EqlSentenceBuilder endOfFunction() {
        return _add(token(END));
    }

    public EqlSentenceBuilder endAsInt() {
        return _add(token(ENDASINT));
    }

    public EqlSentenceBuilder endAsBool() {
        return _add(token(ENDASBOOL));
    }

    public EqlSentenceBuilder endAsStr(final int length) {
        return _add(new EndAsStrToken(length));
    }

    public EqlSentenceBuilder endAsDecimal(final int precision, final int scale) {
        return _add(new EndAsDecimalToken(precision, scale));
    }

    public EqlSentenceBuilder where() {
        return _add(token(WHERE));
    }

    public EqlSentenceBuilder yield() {
        return _add(token(YIELD));
    }

    public EqlSentenceBuilder yieldAll() {
        final EqlSentenceBuilder copy = makeCopy();
        copy.tokens.add(token(YIELDALL));
        copy.yieldAll = true;
        return copy;
    }

    public EqlSentenceBuilder groupBy() {
        return _add(token(GROUPBY));
    }

    public EqlSentenceBuilder orderBy() {
        return _add(token(ORDERBY));
    }

    public EqlSentenceBuilder joinAlias(final String alias) {
        return _add(new AsToken(alias));
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder from() {
        this.mainSourceType = EntityAggregates.class;
        return _add(SelectToken.values());
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder from(final Class<E> entityType) {
        if (entityType == null) {
            throw new IllegalArgumentException("Missing entity type in query: " + tokens.stream().map(Token::getText).collect(joining(" ")));
        }
        this.mainSourceType = entityType;
        return _add(SelectToken.entityType(entityType));
    }

    public EqlSentenceBuilder from(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length == 0) {
            throw new IllegalArgumentException("No models were specified as a source in the FROM statement!");
        }
        this.mainSourceType = EntityAggregates.class;
        return _add(SelectToken.models(List.of(sourceModels)));
    }

    @SafeVarargs
    public final <T extends AbstractEntity<?>> EqlSentenceBuilder from(final EntityResultQueryModel<T>... sourceModels) {
        if (sourceModels.length == 0) {
            throw new IllegalArgumentException("No models were specified as a source in the FROM statement!");
        }
        this.mainSourceType = sourceModels[0].getResultType();
        return _add(SelectToken.models(List.of(sourceModels)));
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder innerJoin(final Class<E> entityType) {
        throw new UnsupportedOperationException();
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder leftJoin(final Class<E> entityType) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder innerJoin(final AggregatedResultQueryModel... sourceModels) {
        throw new UnsupportedOperationException();
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder innerJoin(final EntityResultQueryModel<E>... sourceModels) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder leftJoin(final AggregatedResultQueryModel... sourceModels) {
        throw new UnsupportedOperationException();
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder leftJoin(final EntityResultQueryModel<E>... sourceModels) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder model() {
        return _add(token(MODEL));
    }

    public EqlSentenceBuilder modelAsEntity(final Class<? extends AbstractEntity<?>> type) {
        return _add(new ModelAsEntityToken(type));
    }

    public EqlSentenceBuilder modelAsAggregate() {
        return _add(token(MODELASAGGREGATE));
    }

    public EqlSentenceBuilder modelAsPrimitive() {
        return _add(token(MODELASPRIMITIVE));
    }

    // -----------------------------------------------------------------------------------------------------------------

    public List<? extends Token> getTokens() {
        return unmodifiableList(tokens);
    }

    public ListTokenSource getTokenSource() {
        return new ListTokenSource(tokens);
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
        result = prime * result + tokens.hashCode();
        result = prime * result + (yieldAll ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EqlSentenceBuilder that &&
                Objects.equals(this.mainSourceType, that.mainSourceType) &&
                Objects.equals(this.yieldAll, that.yieldAll) &&
                Objects.equals(this.tokens, that.tokens);
    }

}
