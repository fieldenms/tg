package ua.com.fielden.platform.entity.query.fluent;

import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.*;
import ua.com.fielden.platform.eql.antlr.ListTokenSource;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.processors.metamodel.IConvertableToPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder or() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder beginCondition(final boolean negated) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder endCondition() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder beginExpression() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder endExpression() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder exists(final boolean negated, final QueryModel model) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder existsAnyOf(final boolean negated, final QueryModel... subQueries) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder existsAllOf(final boolean negated, final QueryModel... subQueries) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder critCondition(final String propName, final String critPropName) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder critCondition(final ICompoundCondition0<?> collectionQueryStart, final String propName, final String critPropName, final Optional<Object> defaultValue) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder isNull(final boolean negated) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder lt() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder gt() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder le() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder ge() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder eq() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder ne() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder like(final LikeOptions options) {
        // TODO options
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder in(final boolean negated) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder yield(final String yieldName) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder prop(final String propName) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder extProp(final String propName) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder val(final Object value) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder iVal(final Object value) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder model(final PrimitiveResultQueryModel model) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder model(final SingleResultQueryModel model) {
        return _add(new ModelToken(model));
    }

    public EqlSentenceBuilder param(final String paramName) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder iParam(final String paramName) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder expr() {
        return _add(token(EXPR));
    }

    public EqlSentenceBuilder expr(final ExpressionModel exprModel) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder cond() {
        return _add(token(COND));
    }

    public EqlSentenceBuilder cond(final ConditionModel conditionModel) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder negatedCond(final ConditionModel conditionModel) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder as(final String yieldAlias) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder asRequired(final String yieldAlias) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder anyOfProps(final String... props) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder anyOfProps(final IConvertableToPath... props) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder anyOfParams(final String... params) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder anyOfIParams(final String... params) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder anyOfModels(final PrimitiveResultQueryModel... models) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder anyOfValues(final Object... values) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder anyOfExpressions(final ExpressionModel... expressions) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder allOfProps(final String... props) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder allOfProps(final IConvertableToPath... props) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder allOfParams(final String... params) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder allOfIParams(final String... params) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder allOfModels(final PrimitiveResultQueryModel... models) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder allOfValues(final Object... values) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder allOfExpressions(final ExpressionModel... expressions) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder any(final SingleResultQueryModel subQuery) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder all(final SingleResultQueryModel subQuery) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder setOfProps(final String... props) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder setOfProps(final IConvertableToPath... props) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder setOfParams(final String... params) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder setOfIParams(final String... params) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder setOfValues(final Object... values) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder setOfExpressions(final ExpressionModel... expressions) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder addDateInterval() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder countDateIntervalFunction() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder secondsInterval() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder minutesInterval() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder hoursInterval() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder daysInterval() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder monthsInterval() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder yearsInterval() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder caseWhenFunction() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder then() {
        return _add(token(THEN));
    }

    public EqlSentenceBuilder otherwise() {
        return _add(token(OTHERWISE));
    }

    public EqlSentenceBuilder concat() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder round() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder to(final Integer precision) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder ifNull() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder uppercase() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder lowercase() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder now() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder secondOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder minuteOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder hourOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder dayOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder monthOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder yearOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder dayOfWeekOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder dateOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder absOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder sumOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder maxOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder minOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder countOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder averageOf() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder sumOfDistinct() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder countOfDistinct() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder averageOfDistinct() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder countAll() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder add() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder subtract() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder divide() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder multiply() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder modulo() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder order(OrderingModel order) {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder asc() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder desc() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder on() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder conditionStart() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder yield() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder yieldAll() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder groupBy() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder orderBy() {
        throw new UnsupportedOperationException();
    }

    public EqlSentenceBuilder joinAlias(final String alias) {
        throw new UnsupportedOperationException();
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
