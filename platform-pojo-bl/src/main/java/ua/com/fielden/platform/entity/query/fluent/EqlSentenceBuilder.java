package ua.com.fielden.platform.entity.query.fluent;

import com.google.common.collect.ImmutableList;
import org.antlr.v4.runtime.Token;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.exceptions.EqlValidationException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.model.*;
import ua.com.fielden.platform.eql.antlr.tokens.*;
import ua.com.fielden.platform.eql.antlr.tokens.util.ListTokenSource;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.entity.query.exceptions.EqlException.requireNotNullArgument;
import static ua.com.fielden.platform.entity.query.exceptions.EqlValidationException.ERR_LIMIT_GREATER_THAN_ZERO;
import static ua.com.fielden.platform.entity.query.exceptions.EqlValidationException.ERR_OFFSET_NON_NEGATIVE;
import static ua.com.fielden.platform.eql.antlr.EQLLexer.*;
import static ua.com.fielden.platform.eql.antlr.tokens.IValToken.iValToken;
import static ua.com.fielden.platform.eql.antlr.tokens.ValToken.valToken;
import static ua.com.fielden.platform.eql.antlr.tokens.util.SimpleTokens.token;

/**
 * Builds a sentence in the EQL language out of {@linkplain Token ANTLR tokens}.
 * Classes that implement EQL's DSL (fluent API) delegate to this builder.
 *
 * @author TG Team
 */
final class EqlSentenceBuilder {
    public static final String ERR_NO_MODELS_WERE_SPECIFIED_AS_A_SOURCE_IN_THE_FROM_STATEMENT = "No models were specified as a source in the FROM statement!";
    private final List<Token> tokens;
    private final State state;

    public EqlSentenceBuilder() {
        this(new ArrayList<>(0), new State(null, false, new ValuePreprocessor()));
    }

    private EqlSentenceBuilder(final List<Token> tokens, final State state) {
        requireNotNullArgument(tokens, "tokens");
        requireNotNullArgument(state, "state");
        this.tokens = tokens;
        this.state = state;
    }

    @Override
    public String toString() {
        return tokens.toString();
    }

    private EqlSentenceBuilder makeCopy() {
        final List<Token> newTokens = new ArrayList<>(tokens.size() + 1);
        newTokens.addAll(tokens);
        return new EqlSentenceBuilder(newTokens, state);
    }

    private EqlSentenceBuilder makeCopy(final State newState) {
        final List<Token> newTokens = new ArrayList<>(tokens.size() + 1);
        newTokens.addAll(tokens);
        return new EqlSentenceBuilder(newTokens, newState);
    }

    private EqlSentenceBuilder _add(final Token token) {
        final EqlSentenceBuilder copy = makeCopy();
        copy.tokens.add(token);
        return copy;
    }

    private EqlSentenceBuilder _add(final Token token, final State newState) {
        final EqlSentenceBuilder copy = makeCopy(newState);
        copy.tokens.add(token);
        return copy;
    }

    private static List<String> asStrings(final Collection<? extends CharSequence> charSequences) {
        return charSequences.stream().map(CharSequence::toString).collect(toImmutableList());
    }

    private static List<String> asStrings(final CharSequence... charSequences) {
        return Arrays.stream(charSequences).map(CharSequence::toString).collect(toImmutableList());
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

    public EqlSentenceBuilder beginYieldExpression() {
        return _add(token(BEGINYIELDEXPR));
    }

    public EqlSentenceBuilder endYieldExpression() {
        return _add(token(ENDYIELDEXPR));
    }

    public EqlSentenceBuilder exists(final boolean negated, final QueryModel<?> model) {
        return _add(negated ? new NotExistsToken(model) : new ExistsToken(model));
    }

    public EqlSentenceBuilder existsAnyOf(final boolean negated, final Collection<? extends QueryModel<?>> subQueries) {
        return _add(negated ? new NotExistsAnyOfToken(subQueries) : new ExistsAnyOfToken(subQueries));
    }

    public EqlSentenceBuilder existsAllOf(final boolean negated, final Collection<? extends QueryModel<?>> subQueries) {
        return _add(negated ? new NotExistsAllOfToken(subQueries) : new ExistsAllOfToken(subQueries));
    }

    public EqlSentenceBuilder critCondition(final CharSequence propName, final CharSequence critPropName) {
        return _add(new CritConditionToken(propName.toString(), critPropName.toString()));
    }

    public EqlSentenceBuilder critCondition(
            final ICompoundCondition0<?> collectionQueryStart,
            final CharSequence propName,
            final CharSequence critPropName,
            final Optional<?> defaultValue)
    {
        return _add(new CritConditionToken(collectionQueryStart, propName.toString(), critPropName.toString(), defaultValue));
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

    public EqlSentenceBuilder like() {
        return _add(token(LIKE));
    }

    public EqlSentenceBuilder notLike() {
        return _add(token(NOTLIKE));
    }

    public EqlSentenceBuilder iLike() {
        return _add(token(ILIKE));
    }

    public EqlSentenceBuilder notILike() {
        return _add(token(NOTILIKE));
    }

    public EqlSentenceBuilder likeWithCast() {
        return _add(token(LIKEWITHCAST));
    }

    public EqlSentenceBuilder notLikeWithCast() {
        return _add(token(NOTLIKEWITHCAST));
    }

    public EqlSentenceBuilder iLikeWithCast() {
        return _add(token(ILIKEWITHCAST));
    }

    public EqlSentenceBuilder notILikeWithCast() {
        return _add(token(NOTILIKEWITHCAST));
    }

    public EqlSentenceBuilder in(final boolean negated) {
        return _add(negated ? token(NOTIN) : token(IN));
    }

    public EqlSentenceBuilder yield(final CharSequence yieldName) {
        return _add(new YieldToken(yieldName.toString()));
    }

    public EqlSentenceBuilder prop(final CharSequence propName) {
        return _add(new PropToken(propName.toString()));
    }

    public EqlSentenceBuilder extProp(final CharSequence propName) {
        return _add(new ExtPropToken(propName.toString()));
    }

    public EqlSentenceBuilder val(final Object value) {
        return _add(valToken(state.valuePreprocessor.apply(value)));
    }

    public EqlSentenceBuilder iVal(final Object value) {
        return _add(iValToken(state.valuePreprocessor.apply(value)));
    }

    public EqlSentenceBuilder model(final PrimitiveResultQueryModel model) {
        return _add(new QueryModelToken<>(model));
    }

    public EqlSentenceBuilder model(final SingleResultQueryModel<?> model) {
        return _add(new QueryModelToken<>(model));
    }

    public EqlSentenceBuilder param(final CharSequence paramName) {
        return _add(new ParamToken(paramName.toString()));
    }

    public EqlSentenceBuilder iParam(final CharSequence paramName) {
        return _add(new IParamToken(paramName.toString()));
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

    public EqlSentenceBuilder as(final CharSequence yieldAlias) {
        return _add(new AsToken(yieldAlias.toString()));
    }

    public EqlSentenceBuilder asRequired(final CharSequence yieldAlias) {
        return _add(new AsRequiredToken(yieldAlias.toString()));
    }

    public EqlSentenceBuilder anyOfProps(final CharSequence... props) {
        return _add(new AnyOfPropsToken(asStrings(props)));
    }

    public EqlSentenceBuilder anyOfProps(final Collection<? extends CharSequence> props) {
        return _add(new AnyOfPropsToken(asStrings(props)));
    }

    public EqlSentenceBuilder anyOfParams(final CharSequence... params) {
        return _add(new AnyOfParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder anyOfParams(final Collection<? extends CharSequence> params) {
        return _add(new AnyOfParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder anyOfIParams(final CharSequence... params) {
        return _add(new AnyOfIParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder anyOfIParams(final Collection<? extends CharSequence> params) {
        return _add(new AnyOfIParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder anyOfModels(final Collection<? extends PrimitiveResultQueryModel> models) {
        return _add(new AnyOfModelsToken(models));
    }

    public EqlSentenceBuilder anyOfValues(final Collection<?> values) {
        return _add(new AnyOfValuesToken(state.valuePreprocessor.apply(values).toList()));
    }

    public EqlSentenceBuilder anyOfExpressions(final Collection<? extends ExpressionModel> expressions) {
        return _add(new AnyOfExpressionsToken(expressions));
    }

    public EqlSentenceBuilder allOfProps(final CharSequence... props) {
        return _add(new AllOfPropsToken(asStrings(props)));
    }

    public EqlSentenceBuilder allOfProps(final Collection<? extends CharSequence> props) {
        return _add(new AllOfPropsToken(asStrings(props)));
    }

    public EqlSentenceBuilder allOfParams(final CharSequence... params) {
        return _add(new AllOfParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder allOfParams(final Collection<? extends CharSequence> params) {
        return _add(new AllOfParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder allOfIParams(final CharSequence... params) {
        return _add(new AllOfIParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder allOfIParams(final Collection<? extends CharSequence> params) {
        return _add(new AllOfIParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder allOfModels(final Collection<? extends PrimitiveResultQueryModel> models) {
        return _add(new AllOfModelsToken(models));
    }

    public EqlSentenceBuilder allOfValues(final Collection<?> values) {
        return _add(new AllOfValuesToken(state.valuePreprocessor.apply(values).toList()));
    }

    public EqlSentenceBuilder allOfExpressions(final Collection<? extends ExpressionModel> expressions) {
        return _add(new AllOfExpressionsToken(expressions));
    }

    public EqlSentenceBuilder any(final SingleResultQueryModel<?> subQuery) {
        return _add(new AnyToken(subQuery));
    }

    public EqlSentenceBuilder all(final SingleResultQueryModel<?> subQuery) {
        return _add(new AllToken(subQuery));
    }

    public EqlSentenceBuilder setOfProps(final CharSequence... props) {
        return _add(new PropsToken(asStrings(props)));
    }

    public EqlSentenceBuilder setOfProps(final Collection<? extends CharSequence> props) {
        return _add(new PropsToken(asStrings(props)));
    }

    public EqlSentenceBuilder setOfParams(final CharSequence... params) {
        return _add(new ParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder setOfParams(final Collection<? extends CharSequence> params) {
        return _add(new ParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder setOfIParams(final CharSequence... params) {
        return _add(new IParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder setOfIParams(final Collection<? extends CharSequence> params) {
        return _add(new IParamsToken(asStrings(params)));
    }

    public EqlSentenceBuilder setOfValues(final Collection<?> values) {
        return _add(new ValuesToken(state.valuePreprocessor.apply(values).toList()));
    }

    // TODO remove?
    /**
     * <b>Unimplemented</b>
     */
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

    public EqlSentenceBuilder concatOf() {
        return _add(token(CONCATOF));
    }

    public EqlSentenceBuilder separator() {
        return _add(token(SEPARATOR));
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

    public EqlSentenceBuilder order(OrderingModel model) {
        return _add(new OrderToken(model));
    }

    public EqlSentenceBuilder asc() {
        return _add(token(ASC));
    }

    public EqlSentenceBuilder desc() {
        return _add(token(DESC));
    }

    public EqlSentenceBuilder limit(final long limit) {
        if (limit <= 0) {
            throw new EqlValidationException(ERR_LIMIT_GREATER_THAN_ZERO.formatted(limit));
        }
        return _add(LimitToken.limit(limit));
    }

    public EqlSentenceBuilder limit(final Limit limit) {
        requireNotNullArgument(limit, "limit");
        if (limit instanceof Limit.Count (var n) && n <= 0) {
            throw new EqlValidationException(ERR_LIMIT_GREATER_THAN_ZERO.formatted(n));
        }
        return _add(LimitToken.limit(limit));
    }

    public EqlSentenceBuilder offset(final long offset) {
        if (offset < 0) {
            throw new EqlValidationException(ERR_OFFSET_NON_NEGATIVE.formatted(offset));
        }
        return _add(new OffsetToken(offset));
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
        return _add(token(YIELDALL), state.withYieldAll(true));
    }

    public EqlSentenceBuilder groupBy() {
        return _add(token(GROUPBY));
    }

    public EqlSentenceBuilder orderBy() {
        return _add(token(ORDERBY));
    }

    public EqlSentenceBuilder joinAlias(final CharSequence alias) {
        return _add(new AsToken(alias.toString()));
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder from() {
        return _add(SelectToken.values(), state.withMainSourceType(EntityAggregates.class));
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder from(final Class<E> entityType) {
        if (entityType == null) {
            throw new EqlException("Missing entity type in query: " + tokens.stream().map(Token::getText).collect(joining(" ")));
        }
        return _add(SelectToken.entityType(entityType), state.withMainSourceType(entityType));
    }

    public EqlSentenceBuilder from(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length == 0) {
            throw new EqlException(ERR_NO_MODELS_WERE_SPECIFIED_AS_A_SOURCE_IN_THE_FROM_STATEMENT);
        }
        return _add(SelectToken.models(List.of(sourceModels)), state.withMainSourceType(EntityAggregates.class));
    }

    @SafeVarargs
    public final <T extends AbstractEntity<?>> EqlSentenceBuilder from(final EntityResultQueryModel<T>... sourceModels) {
        if (sourceModels.length == 0) {
            throw new EqlException(ERR_NO_MODELS_WERE_SPECIFIED_AS_A_SOURCE_IN_THE_FROM_STATEMENT);
        }
        return _add(SelectToken.models(List.of(sourceModels)), state.withMainSourceType(sourceModels[0].getResultType()));
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder innerJoin(final Class<E> entityType) {
        return _add(JoinToken.entityType(entityType));
    }

    public <E extends AbstractEntity<?>> EqlSentenceBuilder leftJoin(final Class<E> entityType) {
        return _add(LeftJoinToken.entityType(entityType));
    }

    public EqlSentenceBuilder innerJoin(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length >= 1) {
            return _add(JoinToken.models(ImmutableList.copyOf(sourceModels)));
        } else {
            throw new EqlException(ERR_NO_MODELS_WERE_SPECIFIED_AS_A_SOURCE_IN_THE_FROM_STATEMENT);
        }
    }

    @SafeVarargs
    public final <E extends AbstractEntity<?>> EqlSentenceBuilder innerJoin(final EntityResultQueryModel<E>... sourceModels) {
        if (sourceModels.length >= 1) {
            return _add(JoinToken.models(ImmutableList.copyOf(sourceModels)));
        } else {
            throw new EqlException(ERR_NO_MODELS_WERE_SPECIFIED_AS_A_SOURCE_IN_THE_FROM_STATEMENT);
        }
    }

    public EqlSentenceBuilder leftJoin(final AggregatedResultQueryModel... sourceModels) {
        if (sourceModels.length >= 1) {
            return _add(LeftJoinToken.models(ImmutableList.copyOf(sourceModels)));
        } else {
            throw new EqlException(ERR_NO_MODELS_WERE_SPECIFIED_AS_A_SOURCE_IN_THE_FROM_STATEMENT);
        }
    }

    @SafeVarargs
    public final <E extends AbstractEntity<?>> EqlSentenceBuilder leftJoin(final EntityResultQueryModel<E>... sourceModels) {
        if (sourceModels.length >= 1) {
            return _add(LeftJoinToken.models(ImmutableList.copyOf(sourceModels)));
        } else {
            throw new EqlException(ERR_NO_MODELS_WERE_SPECIFIED_AS_A_SOURCE_IN_THE_FROM_STATEMENT);
        }
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
        return new ListTokenSource(unmodifiableList(tokens));
    }

    public Class<? extends AbstractEntity<?>> getMainSourceType() {
        return state.mainSourceType;
    }

    public boolean isYieldAll() {
        return state.yieldAll;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(state.mainSourceType);
        result = prime * result + tokens.hashCode();
        result = prime * result + Boolean.hashCode(state.yieldAll);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof EqlSentenceBuilder that &&
                Objects.equals(this.state.mainSourceType, that.state.mainSourceType) &&
                Objects.equals(this.state.yieldAll, that.state.yieldAll) &&
                Objects.equals(this.tokens, that.tokens);
    }

    private record State(Class<? extends AbstractEntity<?>> mainSourceType,
                         boolean yieldAll,
                         ValuePreprocessor valuePreprocessor) {
        public State withMainSourceType(final Class<? extends AbstractEntity<?>> mainSourceType) {
            return new State(mainSourceType, yieldAll, valuePreprocessor);
        }

        public State withYieldAll(final boolean yieldAll) {
            return new State(mainSourceType, yieldAll, valuePreprocessor);
        }

        public State withValueProcessor(final ValuePreprocessor valuePreprocessor) {
            return new State(mainSourceType, yieldAll, valuePreprocessor);
        }

    }

}
