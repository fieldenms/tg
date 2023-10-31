package ua.com.fielden.platform.eql.stage0;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Optional.empty;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.cond;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.emptyCondition;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.GROUPED_CONDITIONS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IPARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IVAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PROP;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.VAL;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.buildCondition;
import static ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty.queryPropertyParamName;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.N;
import static ua.com.fielden.platform.eql.meta.EqlEntityMetadataGenerator.Y;
import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.enums.Functions;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
import ua.com.fielden.platform.eql.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.functions.AbsOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.AddDateIntervalBuilder;
import ua.com.fielden.platform.eql.stage0.functions.AverageOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.CaseFunctionBuilder;
import ua.com.fielden.platform.eql.stage0.functions.ConcatFunctionBuilder;
import ua.com.fielden.platform.eql.stage0.functions.CountDateIntervalBuilder;
import ua.com.fielden.platform.eql.stage0.functions.CountOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.DateOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.DayOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.DayOfWeekOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.HourOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.IfNullBuilder;
import ua.com.fielden.platform.eql.stage0.functions.LowerCaseOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.MaxOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.MinOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.MinuteOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.MonthOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.RoundToBuilder;
import ua.com.fielden.platform.eql.stage0.functions.SecondOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.SumOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.UpperCaseOfBuilder;
import ua.com.fielden.platform.eql.stage0.functions.YearOfBuilder;
import ua.com.fielden.platform.eql.stage1.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.OperandsBasedSet1;
import ua.com.fielden.platform.eql.stage1.operands.Prop1;
import ua.com.fielden.platform.eql.stage1.operands.QueryBasedSet1;
import ua.com.fielden.platform.eql.stage1.operands.Value1;
import ua.com.fielden.platform.eql.stage1.operands.functions.CountAll1;
import ua.com.fielden.platform.eql.stage2.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.utils.Pair;

/**
 * Abstract builder to accumulate tokens until ready for respective model creation.
 *
 * @author TG Team
 *
 */
public abstract class AbstractTokensBuilder implements ITokensBuilder {
    private final ITokensBuilder parent;
    private ITokensBuilder child;
    private final List<Pair<TokenCategory, Object>> tokens = new ArrayList<>();
    private final QueryModelToStage1Transformer queryBuilder;

    protected AbstractTokensBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        this.parent = parent;
        this.queryBuilder = queryBuilder;
    }

    private void add(final Functions function) {
        switch (function) {
        case SUM:
            setChild(new SumOfBuilder(this, queryBuilder, false));
            break;
        case COUNT:
            setChild(new CountOfBuilder(this, queryBuilder, false));
            break;
        case AVERAGE:
            setChild(new AverageOfBuilder(this, queryBuilder, false));
            break;
        case MIN:
            setChild(new MinOfBuilder(this, queryBuilder));
            break;
        case MAX:
            setChild(new MaxOfBuilder(this, queryBuilder));
            break;
        case SECOND:
            setChild(new SecondOfBuilder(this, queryBuilder));
            break;
        case MINUTE:
            setChild(new MinuteOfBuilder(this, queryBuilder));
            break;
        case HOUR:
            setChild(new HourOfBuilder(this, queryBuilder));
            break;
        case DAY:
            setChild(new DayOfBuilder(this, queryBuilder));
            break;
        case MONTH:
            setChild(new MonthOfBuilder(this, queryBuilder));
            break;
        case YEAR:
            setChild(new YearOfBuilder(this, queryBuilder));
            break;
        case DAY_OF_WEEK:
            setChild(new DayOfWeekOfBuilder(this, queryBuilder));
            break;
        case DATE:
            setChild(new DateOfBuilder(this, queryBuilder));
            break;
        case ABS:
            setChild(new AbsOfBuilder(this, queryBuilder));
            break;
        case SUM_DISTINCT:
            setChild(new SumOfBuilder(this, queryBuilder, true));
            break;
        case COUNT_DISTINCT:
            setChild(new CountOfBuilder(this, queryBuilder, true));
            break;
        case AVERAGE_DISTINCT:
            setChild(new AverageOfBuilder(this, queryBuilder, true));
            break;
        case UPPERCASE:
            setChild(new UpperCaseOfBuilder(this, queryBuilder));
            break;
        case LOWERCASE:
            setChild(new LowerCaseOfBuilder(this, queryBuilder));
            break;
        case IF_NULL:
            setChild(new IfNullBuilder(this, queryBuilder));
            break;
        case ADD_DATE_INTERVAL:
            setChild(new AddDateIntervalBuilder(this, queryBuilder));
            break;
        case COUNT_DATE_INTERVAL:
            setChild(new CountDateIntervalBuilder(this, queryBuilder));
            break;
        case CASE_WHEN:
            setChild(new CaseFunctionBuilder(this, queryBuilder));
            break;
        case ROUND:
            setChild(new RoundToBuilder(this, queryBuilder));
            break;
        case CONCAT:
            setChild(new ConcatFunctionBuilder(this, queryBuilder));
            break;
        default:
            throw new RuntimeException("Unrecognised function token: " + function);
        }
    }

    @Override
    public void add(final TokenCategory cat, final Object value) {
        if (child != null) {
            child.add(cat, value);
        } else {
            switch (cat) {
            case BEGIN_EXPR: //eats token
                setChild(new ExpressionBuilder(this, queryBuilder));
                break;
            case FUNCTION: //eats token
            case COLLECTIONAL_FUNCTION: //eats token
                add((Functions) value);
                break;
            case BEGIN_COND: //eats token
                setChild(new GroupedConditionsBuilder(this, queryBuilder, (Boolean) value));
                break;
            case CRIT_COND_OPERATOR: //
                tokens.add(pair(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, critConditionOperatorModel((Pair<Object, Object>) value), false).getModel()));
                break;
            case COND_TOKENS: //
                tokens.add(pair(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, (ConditionModel) value, false).getModel()));
                break;
            case NEGATED_COND_TOKENS: //
                tokens.add(pair(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, (ConditionModel) value, true).getModel()));
                break;
            case LOGICAL_OPERATOR:
                setChild(new CompoundConditionBuilder(this, queryBuilder, cat, value));
                break;
            default:
                tokens.add(pair(cat, value));
                break;
            }

            if (isClosing()) {
                parent.finaliseChild();
            }
        }
    }

    private ConditionModel critConditionOperatorModel(final Pair<Object, Object> props) {
        final String critOnlyPropName = props.getValue() instanceof String ? (String) props.getValue() : ((T2<String, Optional<Object>>) props.getValue())._1;
        final String critOnlyPropParamName = queryPropertyParamName(critOnlyPropName);
        final QueryProperty qp = (QueryProperty) getParamValue(critOnlyPropParamName);
        if (qp != null && qp.isEmptyWithoutMnemonics()) {
            final Optional<Object> maybeDefaultValue = props.getValue() instanceof T2 ? ((T2<String, Optional<Object>>) props.getValue())._2 : empty();
            maybeDefaultValue.ifPresent(dv -> {
                if (dv instanceof List || dv instanceof String) {
                    qp.setValue(dv);
                }
                else if (dv instanceof T2) {
                    final T2<?,?> t2 = (T2<?,?>) dv;
                    qp.setValue(t2._1);
                    qp.setValue2(t2._2);
                }
                else {
                    throw new EqlException(format("Default value for property [%s] in a [critCondition] call has unsupported type [%s].", critOnlyPropName, dv.getClass()));
                }
            });
        }
        if (qp == null || qp.isEmptyWithoutMnemonics()) {
            return emptyCondition();
        } else if (props.getKey() instanceof String) {
            return buildCondition(qp, (String) props.getKey(), false, queryBuilder.nowValue.dates);
        } else {
            final T2<ICompoundCondition0<?>, String> args =  (T2<ICompoundCondition0<?>, String>) props.getKey();
            return collectionalCritConditionOperatorModel(args._1, args._2, qp);
        }
    }


    private ConditionModel prepareCollectionalCritCondition(final QueryProperty qp, final String propName) {
        final Boolean originalOrNull = qp.getOrNull();
        final Boolean originalNot = qp.getNot();
        qp.setOrNull(null);
        qp.setNot(null);
        final ConditionModel result = qp == null || qp.isEmptyWithoutMnemonics() ? emptyCondition() : buildCondition(qp, propName, false, queryBuilder.nowValue.dates);
        qp.setOrNull(originalOrNull);
        qp.setNot(originalNot);
        return result;
    }

    /**
     * The following rules are used to build {@code ConditionModel}.
     * <pre>
     * v n m
     * + + +  not (exists collectional element that matches any of the values || empty) == there are no collectional elements that match any of values && not empty
     * + + -  not (exists collectional element that matches any of the values && not empty) == there are no collectional elements that match any of values || empty
     * + - +  exists collectional element that matches any of the values || empty
     * + - -  exists collectional element that matches any of the values && not empty
     * - + +  not empty
     * - + -  no condition
     * - - +  empty
     * - - -  no condition
     * </pre>
     */
    private ConditionModel collectionalCritConditionOperatorModel(final ICompoundCondition0<?> collectionQueryStart, final String propName, final QueryProperty qp) {
        final boolean hasValue = !qp.isEmpty();
        final boolean not = TRUE.equals(qp.getNot());
        final boolean orNull = TRUE.equals(qp.getOrNull());

        final ConditionModel criteriaCondition = prepareCollectionalCritCondition(qp, propName);
        final EntityResultQueryModel<?> anyItems = collectionQueryStart.model();
        final EntityResultQueryModel<?> matchingItems = collectionQueryStart.and().condition(criteriaCondition).model();

        if (!hasValue) {
            return !orNull ? emptyCondition()/*---,-+-*/ : (not ? cond().exists(anyItems).model()/*-++*/ : cond().notExists(anyItems).model())/*--+*/;
        } else if (not){
            return orNull ? cond().notExists(matchingItems).and().exists(anyItems).model()/*+++*/ : cond().notExists(matchingItems).or().notExists(anyItems).model()/*++-*/;
        } else {
            return !orNull ? cond().exists(matchingItems).model()/*+--*/ : cond().exists(matchingItems).or().notExists(anyItems).model()/*+-+*/;
        }
    }

    @Override
    public void finaliseChild() {
        if (child != null) {
            final ITokensBuilder last = child;
            setChild(null);
            final Pair<TokenCategory, Object> result = last.getResult();
            add(result.getKey(), result.getValue());
        }
    }

    public TokenCategory firstCat() {
        return tokens.isEmpty() ? null : tokens.get(0).getKey();
    }

    public TokenCategory secondCat() {
        return tokens.size() < 2 ? null : tokens.get(1).getKey();
    }

    public TokenCategory thirdCat() {
        return tokens.size() < 3 ? null : tokens.get(2).getKey();
    }

    public <V> V firstValue() {
        return tokens.isEmpty() ? null : (V) tokens.get(0).getValue();
    }

    public <V> V secondValue() {
        return tokens.size() < 2 ? null : (V) tokens.get(1).getValue();
    }

    public <V> V thirdValue() {
        return tokens.size() < 3 ? null : (V) tokens.get(2).getValue();
    }

    protected List<Pair<TokenCategory, Object>> getTokens() {
        return tokens;
    }

    protected int getSize() {
        return tokens.size();
    }

    protected TokenCategory getLastCat() {
        return !tokens.isEmpty() ? tokens.get(tokens.size() - 1).getKey() : null;
    }

    protected Object getLastValue() {
        return !tokens.isEmpty() ? tokens.get(tokens.size() - 1).getValue() : null;
    }

    protected ITokensBuilder getChild() {
        return child;
    }

    protected void setChild(final ITokensBuilder child) {
        this.child = child;
    }

    protected ISingleOperand1<? extends ISingleOperand2<?>> getZeroArgFunctionModel(final Functions function) {
        switch (function) {
        case COUNT_ALL:
            return CountAll1.INSTANCE;
        case NOW:
            final QueryNowValue qnv = queryBuilder.nowValue;
            return new Value1(qnv != null ? qnv.get() : null);
        default:
            throw new RuntimeException("Unrecognised zero agrument function: " + function);
        }
    }

    protected ISingleOperand1<? extends ISingleOperand2<?>> getModelForSingleOperand(final TokenCategory cat, final Object value) {
        switch (cat) {
        case PROP:
            return new Prop1((String) value, false);
        case EXT_PROP:
            return new Prop1((String) value, true);
        case PARAM:
            return new Value1(getParamValue((String) value));
        case IPARAM:
            return new Value1(getParamValue((String) value), true);
        case VAL:
            return new Value1(preprocessValue(value));
        case IVAL:
            return new Value1(preprocessValue(value), true);
        case ZERO_ARG_FUNCTION:
            return getZeroArgFunctionModel((Functions) value);
        case EXPR:
        case FUNCTION_MODEL:
            return (ISingleOperand1<? extends ISingleOperand2<?>>) value;
        case EXPR_TOKENS:
            return (ISingleOperand1<? extends ISingleOperand2<?>>) new StandAloneExpressionBuilder(queryBuilder, (ExpressionModel) value).getResult().getValue();
        case EQUERY_TOKENS:
            return queryBuilder.generateAsSubQuery((QueryModel<?>) value);
        default:
            throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
        }
    }

    protected List<ISingleOperand1<? extends ISingleOperand2<?>>> getModelForArrayParam(final TokenCategory cat, final Object value) {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> result = new ArrayList<>();
        final Object paramValue = getParamValue((String) value);

        if (!(paramValue instanceof List)) {
            result.add(getModelForSingleOperand(cat, value));
        } else {
            for (final Object singleValue : (List<Object>) paramValue) {
                result.add(getModelForSingleOperand((cat == IPARAM ? IVAL : VAL), singleValue));
            }
        }
        return result;
    }

    protected Object getParamValue(final String paramName) {
        final Object paramValue = queryBuilder.getParamValue(paramName);
        if (paramValue != null) {
            return preprocessValue(paramValue);
        } else {
            return null; //TODO think through
            //throw new RuntimeException("No value has been provided for parameter with name [" + paramName + "]");
        }
    }

    private Object preprocessValue(final Object value) {
        if (value != null && (value.getClass().isArray() || value instanceof Collection<?>)) {
            final Iterable<?> iterable = value.getClass().isArray() ? Arrays.asList((Object[]) value) : (Collection<?>) value;
            final List<Object> values = new ArrayList<>();
            for (final Object object : iterable) {
                final Object furtherPreprocessed = preprocessValue(object);
                if (furtherPreprocessed instanceof List) {
                    values.addAll((List<?>) furtherPreprocessed);
                } else {
                    values.add(furtherPreprocessed);
                }
            }
            return values;
        } else {
            return convertValue(value);
        }
    }

    /** Ensures that values of boolean types are converted properly. */
    private Object convertValue(final Object value) {
        if (value instanceof Boolean) {
            return (boolean) value ? Y : N;
        }
        return value;
    }

    protected ISetOperand1<? extends ISetOperand2<?>> getModelForSetOperand(final TokenCategory cat, final Object value) {
        TokenCategory singleCat;

        switch (cat) {
        case SET_OF_PROPS:
            singleCat = PROP;
            break;
        case SET_OF_VALUES:
            singleCat = VAL;
            break;
        case SET_OF_PARAMS:
            singleCat = PARAM;
            break;
        case SET_OF_IPARAMS:
            singleCat = IPARAM;
            break;
        case SET_OF_EXPR_TOKENS:
            singleCat = EXPR_TOKENS;
            break;
        case EQUERY_TOKENS:
            return new QueryBasedSet1(queryBuilder.generateAsSubQuery((QueryModel) value));
        default:
            throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
        }

        final List<ISingleOperand1<? extends ISingleOperand2<?>>> result = new ArrayList<>();

        for (final Object singleValue : (List<Object>) value) {
            if (singleCat == PARAM || singleCat == IPARAM) {
                result.addAll(getModelForArrayParam(singleCat, singleValue));
            } else {
                result.add(getModelForSingleOperand(singleCat, singleValue));
            }
        }

        return new OperandsBasedSet1(result);
    }

    protected ISingleOperand1<? extends ISingleOperand2<?>> getModelForSingleOperand(final Pair<TokenCategory, Object> pair) {
        return getModelForSingleOperand(pair.getKey(), pair.getValue());
    }

    protected List<ISingleOperand1<? extends ISingleOperand2<?>>> getModelForMultipleOperands(final TokenCategory cat, final Object value) {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> result = new ArrayList<>();

        final TokenCategory singleCat;

        switch (cat) {
        case ANY_OF_PROPS:
        case ALL_OF_PROPS:
            singleCat = PROP;
            break;
        case ANY_OF_PARAMS:
        case ALL_OF_PARAMS:
            singleCat = PARAM;
            break;
        case ANY_OF_IPARAMS:
        case ALL_OF_IPARAMS:
            singleCat = IPARAM;
            break;
        case ANY_OF_VALUES:
        case ALL_OF_VALUES:
            singleCat = VAL;
            break;
        case ANY_OF_EQUERY_TOKENS:
        case ALL_OF_EQUERY_TOKENS:
            singleCat = EQUERY_TOKENS;
            break;
        default:
            throw new EqlStage1ProcessingException(format("Unrecognised token category [%s] for MultipleOperand.", cat));
        }

        for (final Object singleValue : (List<Object>) value) {
            if (singleCat == PARAM || singleCat == IPARAM) {
                result.addAll(getModelForArrayParam(singleCat, singleValue));
            } else {
                result.add(getModelForSingleOperand(singleCat, singleValue));
            }
        }

        return result;
    }

    protected QueryModelToStage1Transformer getQueryBuilder() {
        return queryBuilder;
    }
}