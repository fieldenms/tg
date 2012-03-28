package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.fluent.Functions;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.CountAll;
import ua.com.fielden.platform.entity.query.generation.elements.EntProp;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntValue;
import ua.com.fielden.platform.entity.query.generation.elements.ISetOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.Now;
import ua.com.fielden.platform.entity.query.generation.elements.OperandsBasedSet;
import ua.com.fielden.platform.entity.query.generation.elements.QueryBasedSet;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
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
    private final List<Pair<TokenCategory, Object>> tokens = new ArrayList<Pair<TokenCategory, Object>>();
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
	case COUNT_DAYS:
	    setChild(new CountDaysBuilder(this, queryBuilder, getParamValues()));
	    break;
	case CASE_WHEN:
	    setChild(new CaseFunctionBuilder(this, queryBuilder, getParamValues()));
	    break;
	case ROUND:
	    setChild(new RoundToBuilder(this, queryBuilder, getParamValues()));
	    break;
	default:
	    throw new RuntimeException("Unrecognised function token: " + function);
	}
    }

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
	    case LOGICAL_OPERATOR:
		setChild(new CompoundConditionBuilder(this, queryBuilder, getParamValues(), cat, value));
		break;
	    default:
		tokens.add(new Pair<TokenCategory, Object>(cat, value));
		break;
	    }

	    if (isClosing()) {
		parent.finaliseChild();
	    }
	}
    }

    public boolean canBeClosed() {
	return isClosing();
    }

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
	return tokens.size() > 0 ? tokens.get(tokens.size() - 1).getKey() : null;
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
	    return new Now();

	default:
	    throw new RuntimeException("Unrecognised zero agrument function: " + function);
	}
    }

    protected ISingleOperand getModelForSingleOperand(final TokenCategory cat, final Object value) {
	switch (cat) {
	case PROP:
	    return new EntProp((String) value);
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

    protected Object getParamValue(final String paramName) {
	if (getParamValues().containsKey(paramName)) {
	    return preprocessValue(getParamValues().get(paramName));
	} else {
	    throw new RuntimeException("No value has been provided for parameter with name [" + paramName + "]");
	}
    }

    private Object preprocessValue (final Object value) {
	if (value != null && value.getClass().isArray()) {
	    final List<Object> values = new ArrayList<Object>();
	    for (final Object object : (Object[]) value) {
		values.add(convertValue(object));
	    }
	    return values.toArray();
	} else {
	    return convertValue(value);
	}
    }

    /** Ensures that values of special types such as {@link Class} or {@link PropertyDescriptor} are converted to String. */
    private Object convertValue(final Object value) {
	if (value instanceof Boolean) {
	    return getQueryBuilder().getDomainPersistenceMetadata().getBooleanValue((Boolean) value);
	}
	return value;
    }

    protected ISetOperand getModelForSetOperand(final TokenCategory cat, final Object value) {
	TokenCategory singleCat;

	switch (cat) {
	case SET_OF_PROPS:
	    singleCat = TokenCategory.PROP;
	    break;
	case SET_OF_VALUES:
	    singleCat = TokenCategory.VAL;
	    break;
	case SET_OF_PARAMS:
	    singleCat = TokenCategory.PARAM;
	    break;
	case SET_OF_EXPR_TOKENS:
	    singleCat = TokenCategory.EXPR_TOKENS;
	    break;
	case EQUERY_TOKENS:
	    return new QueryBasedSet((EntQuery) getModelForSingleOperand(cat, value));
	default:
	    throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
	}

	final List<ISingleOperand> result = new ArrayList<ISingleOperand>();

	for (final Object singleValue : (List<Object>) value) {
	    result.add(getModelForSingleOperand(singleCat, singleValue));
	}

	return new OperandsBasedSet(result);
    }

    protected ISingleOperand getModelForSingleOperand(final Pair<TokenCategory, Object> pair) {
	return getModelForSingleOperand(pair.getKey(), pair.getValue());
    }

    protected List<ISingleOperand> getModelForMultipleOperands(final TokenCategory cat, final Object value) {
	final List<ISingleOperand> result = new ArrayList<ISingleOperand>();

	TokenCategory singleCat;

	switch (cat) {
	case ANY_OF_PROPS:
	case ALL_OF_PROPS:
	    singleCat = TokenCategory.PROP;
	    break;
	case ANY_OF_PARAMS:
	case ALL_OF_PARAMS:
	    singleCat = TokenCategory.PARAM;
	    break;
	case ANY_OF_VALUES:
	case ALL_OF_VALUES:
	    singleCat = TokenCategory.VAL;
	    break;
	case ANY_OF_EQUERY_TOKENS:
	case ALL_OF_EQUERY_TOKENS:
	    singleCat = TokenCategory.EQUERY_TOKENS;
	    break;
	default:
	    throw new RuntimeException("Unrecognised token category for MultipleOperand: " + cat);
	}

	for (final Object singleValue : (List<Object>) value) {
	    result.add(getModelForSingleOperand(singleCat, singleValue));
	}

	return result;
    }

    protected EntQueryGenerator getQueryBuilder() {
	return queryBuilder;
    }

    public Map<String, Object> getParamValues() {
	return paramValues;
    }
}