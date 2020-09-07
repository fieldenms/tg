package ua.com.fielden.platform.entity.query.generation;

import static java.lang.Boolean.TRUE;
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
import static ua.com.fielden.platform.utils.Pair.pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.DbVersion;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.enums.Functions;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CountAll;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntValue;
import ua.com.fielden.platform.entity.query.generation.elements.ISetOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.OperandsBasedSet;
import ua.com.fielden.platform.entity.query.generation.elements.QueryBasedSet;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity_centre.review.DynamicQueryBuilder.QueryProperty;
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
    private final Map<String, Object> paramValues;
    private final EntQueryGenerator queryBuilder;

    protected AbstractTokensBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        this.parent = parent;
        this.queryBuilder = queryBuilder;
        this.paramValues = paramValues;
    }

    private void add(final Functions function) {
        switch (function) {
        case SUM:
            setChild(new SumOfBuilder(this, queryBuilder, getParamValues(), false));
            break;
        case COUNT:
            setChild(new CountOfBuilder(this, queryBuilder, getParamValues(), false));
            break;
        case AVERAGE:
            setChild(new AverageOfBuilder(this, queryBuilder, getParamValues(), false));
            break;
        case MIN:
            setChild(new MinOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case MAX:
            setChild(new MaxOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case SECOND:
            setChild(new SecondOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case MINUTE:
            setChild(new MinuteOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case HOUR:
            setChild(new HourOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case DAY:
            setChild(new DayOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case MONTH:
            setChild(new MonthOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case YEAR:
            setChild(new YearOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case DAY_OF_WEEK:
            setChild(new DayOfWeekOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case DATE:
            setChild(new DateOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case ABS:
            setChild(new AbsOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case SUM_DISTINCT:
            setChild(new SumOfBuilder(this, queryBuilder, getParamValues(), true));
            break;
        case COUNT_DISTINCT:
            setChild(new CountOfBuilder(this, queryBuilder, getParamValues(), true));
            break;
        case AVERAGE_DISTINCT:
            setChild(new AverageOfBuilder(this, queryBuilder, getParamValues(), true));
            break;
        case UPPERCASE:
            setChild(new UpperCaseOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case LOWERCASE:
            setChild(new LowerCaseOfBuilder(this, queryBuilder, getParamValues()));
            break;
        case IF_NULL:
            setChild(new IfNullBuilder(this, queryBuilder, getParamValues()));
            break;
        case ADD_DATE_INTERVAL:
            setChild(new AddDateIntervalBuilder(this, queryBuilder, getParamValues()));
            break;
        case COUNT_DATE_INTERVAL:
            setChild(new CountDateIntervalBuilder(this, queryBuilder, getParamValues()));
            break;
        case CASE_WHEN:
            setChild(new CaseFunctionBuilder(this, queryBuilder, getParamValues()));
            break;
        case ROUND:
            setChild(new RoundToBuilder(this, queryBuilder, getParamValues()));
            break;
        case CONCAT:
            setChild(new ConcatFunctionBuilder(this, queryBuilder, getParamValues()));
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
                setChild(new ExpressionBuilder(this, queryBuilder, getParamValues()));
                break;
            case FUNCTION: //eats token
            case COLLECTIONAL_FUNCTION: //eats token
                add((Functions) value);
                break;
            case BEGIN_COND: //eats token
                setChild(new GroupedConditionsBuilder(this, queryBuilder, getParamValues(), (Boolean) value));
                break;
            case CRIT_COND_OPERATOR: //
                tokens.add(pair(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, getParamValues(), critConditionOperatorModel((Pair<Object, String>) value), false).getModel()));
                break;
            case COND_TOKENS: //
                tokens.add(pair(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, getParamValues(), (ConditionModel) value, false).getModel()));
                break;
            case NEGATED_COND_TOKENS: //
                tokens.add(pair(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, getParamValues(), (ConditionModel) value, true).getModel()));
                break;
            case LOGICAL_OPERATOR:
                setChild(new CompoundConditionBuilder(this, queryBuilder, getParamValues(), cat, value));
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

    private ConditionModel critConditionOperatorModel(final Pair<Object, String> props) {
        final String critOnlyPropName = props.getValue();
        final String critOnlyPropParamName = queryPropertyParamName(critOnlyPropName);
        final QueryProperty qp = (QueryProperty) getParamValue(critOnlyPropParamName);
        if (qp == null || qp.isEmptyWithoutMnemonics()) {
            return emptyCondition();
        } else if (props.getKey() instanceof String) {
            return buildCondition(qp, (String) props.getKey(), false, queryBuilder.dates());
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
        final ConditionModel result = qp == null || qp.isEmptyWithoutMnemonics() ? emptyCondition() : buildCondition(qp, propName, false, queryBuilder.dates());
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
    public boolean canBeClosed() {
        return isClosing();
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

    protected void setChild(final AbstractTokensBuilder child) {
        this.child = child;
    }

    public TokenCategory firstCat() {
        return tokens.size() < 1 ? null : tokens.get(0).getKey();
    }

    public TokenCategory secondCat() {
        return tokens.size() < 2 ? null : tokens.get(1).getKey();
    }

    public TokenCategory thirdCat() {
        return tokens.size() < 3 ? null : tokens.get(2).getKey();
    }

    public Object firstValue() {
        return tokens.size() < 1 ? null : tokens.get(0).getValue();
    }

    public Object secondValue() {
        return tokens.size() < 2 ? null : tokens.get(1).getValue();
    }

    public Object thirdValue() {
        return tokens.size() < 3 ? null : tokens.get(2).getValue();
    }

    public List<Pair<TokenCategory, Object>> getTokens() {
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

    protected ISingleOperand getZeroArgFunctionModel(final Functions function) {
        switch (function) {
        case COUNT_ALL:
            return new CountAll();
        case NOW:
            return new EntValue(getParamValue(EntQueryGenerator.NOW)); //return new Now(getDbVersion());

        default:
            throw new RuntimeException("Unrecognised zero agrument function: " + function);
        }
    }

    protected ISingleOperand getModelForSingleOperand(final TokenCategory cat, final Object value) {
        switch (cat) {
        case PROP:
            return new EntProp((String) value);
        case EXT_PROP:
            return new EntProp((String) value, true);
        case PARAM:
            return new EntValue(getParamValue((String) value));
        case IPARAM:
            return new EntValue(getParamValue((String) value), true);
        case VAL:
            return new EntValue(preprocessValue(value));
        case IVAL:
            return new EntValue(preprocessValue(value), true);
        case ZERO_ARG_FUNCTION:
            return getZeroArgFunctionModel((Functions) value);
        case EXPR:
        case FUNCTION_MODEL:
            return (ISingleOperand) value;
        case EXPR_TOKENS:
            return (ISingleOperand) new StandAloneExpressionBuilder(queryBuilder, getParamValues(), (ExpressionModel) value).getResult().getValue();
        case EQUERY_TOKENS:
        case ALL_OPERATOR:
        case ANY_OPERATOR:
            return queryBuilder.generateEntQueryAsSubquery((QueryModel) value, getParamValues());
        default:
            throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
        }
    }

    protected List<ISingleOperand> getModelForArrayParam(final TokenCategory cat, final Object value) {
        final List<ISingleOperand> result = new ArrayList<>();
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
        if (getParamValues().containsKey(paramName)) {
            return preprocessValue(getParamValues().get(paramName));
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
            return getQueryBuilder().getDomainMetadataAnalyser().getBooleanValue((Boolean) value);
        }
        return value;
    }

    protected ISetOperand getModelForSetOperand(final TokenCategory cat, final Object value) {
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
            return new QueryBasedSet((EntQuery) getModelForSingleOperand(cat, value));
        default:
            throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
        }

        final List<ISingleOperand> result = new ArrayList<>();

        for (final Object singleValue : (List<Object>) value) {
            if (singleCat == PARAM || singleCat == IPARAM) {
                result.addAll(getModelForArrayParam(singleCat, singleValue));
            } else {
                result.add(getModelForSingleOperand(singleCat, singleValue));
            }
        }

        return new OperandsBasedSet(result);
    }

    protected ISingleOperand getModelForSingleOperand(final Pair<TokenCategory, Object> pair) {
        return getModelForSingleOperand(pair.getKey(), pair.getValue());
    }

    protected List<ISingleOperand> getModelForMultipleOperands(final TokenCategory cat, final Object value) {
        final List<ISingleOperand> result = new ArrayList<>();

        TokenCategory singleCat;

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
            throw new RuntimeException("Unrecognised token category for MultipleOperand: " + cat);
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

    protected EntQueryGenerator getQueryBuilder() {
        return queryBuilder;
    }

    public Map<String, Object> getParamValues() {
        return paramValues;
    }

    public DbVersion getDbVersion() {
        return getQueryBuilder().getDbVersion();
    }

}