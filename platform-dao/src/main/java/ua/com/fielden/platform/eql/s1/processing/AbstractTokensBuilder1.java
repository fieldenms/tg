package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.Functions;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.s1.elements.CountAll1;
import ua.com.fielden.platform.eql.s1.elements.EntParam1;
import ua.com.fielden.platform.eql.s1.elements.EntProp1;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.EntValue1;
import ua.com.fielden.platform.eql.s1.elements.ISetOperand1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.Now1;
import ua.com.fielden.platform.eql.s1.elements.OperandsBasedSet1;
import ua.com.fielden.platform.eql.s1.elements.QueryBasedSet1;
import ua.com.fielden.platform.eql.s2.elements.ISetOperand2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.GROUPED_CONDITIONS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.IPARAM;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.IVAL;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.PARAM;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.PROP;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.VAL;
/**
 * Abstract builder to accumulate tokens until ready for respective model creation.
 *
 * @author TG Team
 *
 */
public abstract class AbstractTokensBuilder1 implements ITokensBuilder1 {
    private final ITokensBuilder1 parent;
    private ITokensBuilder1 child;
    private final List<Pair<TokenCategory, Object>> tokens = new ArrayList<Pair<TokenCategory, Object>>();
    private final EntQueryGenerator1 queryBuilder;

    protected AbstractTokensBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder) {
	this.parent = parent;
	this.queryBuilder = queryBuilder;
    }

    private void add(final Functions function) {
	switch (function) {
	case SUM:
	    setChild(new SumOfBuilder1(this, queryBuilder, false));
	    break;
	case COUNT:
	    setChild(new CountOfBuilder1(this, queryBuilder, false));
	    break;
	case AVERAGE:
	    setChild(new AverageOfBuilder1(this, queryBuilder, false));
	    break;
	case MIN:
	    setChild(new MinOfBuilder1(this, queryBuilder));
	    break;
	case MAX:
	    setChild(new MaxOfBuilder1(this, queryBuilder));
	    break;
	case SECOND:
	    setChild(new SecondOfBuilder1(this, queryBuilder));
	    break;
	case MINUTE:
	    setChild(new MinuteOfBuilder1(this, queryBuilder));
	    break;
	case HOUR:
	    setChild(new HourOfBuilder1(this, queryBuilder));
	    break;
	case DAY:
	    setChild(new DayOfBuilder1(this, queryBuilder));
	    break;
	case MONTH:
	    setChild(new MonthOfBuilder1(this, queryBuilder));
	    break;
	case YEAR:
	    setChild(new YearOfBuilder1(this, queryBuilder));
	    break;
	case DATE:
	    setChild(new DateOfBuilder1(this, queryBuilder));
	    break;
	case ABS:
	    setChild(new AbsOfBuilder1(this, queryBuilder));
	    break;
	case SUM_DISTINCT:
	    setChild(new SumOfBuilder1(this, queryBuilder, true));
	    break;
	case COUNT_DISTINCT:
	    setChild(new CountOfBuilder1(this, queryBuilder, true));
	    break;
	case AVERAGE_DISTINCT:
	    setChild(new AverageOfBuilder1(this, queryBuilder, true));
	    break;
	case UPPERCASE:
	    setChild(new UpperCaseOfBuilder1(this, queryBuilder));
	    break;
	case LOWERCASE:
	    setChild(new LowerCaseOfBuilder1(this, queryBuilder));
	    break;
	case IF_NULL:
	    setChild(new IfNullBuilder1(this, queryBuilder));
	    break;
	case COUNT_DATE_INTERVAL:
	    setChild(new CountDateIntervalBuilder1(this, queryBuilder));
	    break;
	case CASE_WHEN:
	    setChild(new CaseFunctionBuilder1(this, queryBuilder));
	    break;
	case ROUND:
	    setChild(new RoundToBuilder1(this, queryBuilder));
	    break;
	case CONCAT:
	    setChild(new ConcatFunctionBuilder1(this, queryBuilder));
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
		setChild(new ExpressionBuilder1(this, queryBuilder));
		break;
	    case FUNCTION: //eats token
	    case COLLECTIONAL_FUNCTION: //eats token
		add((Functions) value);
		break;
	    case BEGIN_COND: //eats token
		setChild(new GroupedConditionsBuilder1(this, queryBuilder, (Boolean) value));
		break;
	    case COND_TOKENS: //
		tokens.add(new Pair<TokenCategory, Object>(GROUPED_CONDITIONS, new StandAloneConditionBuilder1(queryBuilder, (ConditionModel) value, false).getModel()));
		break;
	    case NEGATED_COND_TOKENS: //
		tokens.add(new Pair<TokenCategory, Object>(GROUPED_CONDITIONS, new StandAloneConditionBuilder1(queryBuilder, (ConditionModel) value, true).getModel()));
		break;
	    case LOGICAL_OPERATOR:
		setChild(new CompoundConditionBuilder1(this, queryBuilder, cat, value));
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
	    final ITokensBuilder1 last = child;
	    setChild(null);
	    final Pair<TokenCategory, Object> result = last.getResult();
	    add(result.getKey(), result.getValue());
	}
    }

    protected void setChild(final AbstractTokensBuilder1 child) {
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

    protected ITokensBuilder1 getChild() {
	return child;
    }

    protected void setChild(final ITokensBuilder1 child) {
	this.child = child;
    }

    protected ISingleOperand1 getZeroArgFunctionModel(final Functions function) {
	switch (function) {
	case COUNT_ALL:
	    return new CountAll1();
	case NOW:
	    return new Now1();

	default:
	    throw new RuntimeException("Unrecognised zero agrument function: " + function);
	}
    }

    protected ISingleOperand1<? extends ISingleOperand2> getModelForSingleOperand(final TokenCategory cat, final Object value) {
	switch (cat) {
	case PROP:
	    return new EntProp1((String) value);
	case EXT_PROP:
	    return new EntProp1((String) value, true);
	case PARAM:
	    return new EntParam1((String) value);
	    //return new EntValue1(getParamValue((String) value));
	case IPARAM:
	    return new EntParam1((String) value, true);
	    //return new EntValue1(getParamValue((String) value), true);
	case VAL:
	    return new EntValue1(preprocessValue(value));
	case IVAL:
	    return new EntValue1(preprocessValue(value), true);
	case ZERO_ARG_FUNCTION:
	    return getZeroArgFunctionModel((Functions) value);
	case EXPR:
	case FUNCTION_MODEL:
	    return (ISingleOperand1) value;
	case EXPR_TOKENS:
	    return (ISingleOperand1) new StandAloneExpressionBuilder1(queryBuilder, (ExpressionModel) value).getResult().getValue();
	case EQUERY_TOKENS:
	case ALL_OPERATOR:
	case ANY_OPERATOR:
	    return queryBuilder.generateEntQueryAsSubquery((QueryModel) value);
	default:
	    throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
	}
    }

//    protected List<ISingleOperand1<? extends ISingleOperand2>> getModelForArrayParam(final TokenCategory cat, final Object value) {
//	final List<ISingleOperand1<? extends ISingleOperand2>> result = new ArrayList<>();
//	final Object paramValue = getParamValue((String) value);
//
//	if (!(paramValue instanceof List)) {
//	    result.add(getModelForSingleOperand(cat, value));
//	} else {
//	    for (final Object singleValue : (List<Object>) paramValue) {
//		result.add(getModelForSingleOperand((cat == IPARAM ? IVAL : VAL), singleValue));
//	    }
//	}
//	return result;
//    }
//
//    protected Object getParamValue(final String paramName) {
//	if (getParamValues().containsKey(paramName)) {
//	    return preprocessValue(getParamValues().get(paramName));
//	} else {
//	    return null; //TODO think through
//	    //throw new RuntimeException("No value has been provided for parameter with name [" + paramName + "]");
//	}
//    }

    private Object preprocessValue(final Object value) {
	if (value != null && (value.getClass().isArray() || value instanceof Collection<?>)) {
	    final List<Object> values = new ArrayList<Object>();
	    for (final Object object : (Iterable) value) {
		final Object furtherPreprocessed = preprocessValue(object);
		if (furtherPreprocessed instanceof List) {
		    values.addAll((List) furtherPreprocessed);
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
	    return getQueryBuilder().getDomainMetadataAnalyser().getDomainMetadata().getBooleanValue((Boolean) value);
	}
	return value;
    }

    protected ISetOperand1<? extends ISetOperand2> getModelForSetOperand(final TokenCategory cat, final Object value) {
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
	    return new QueryBasedSet1((EntQuery1) getModelForSingleOperand(cat, value));
	default:
	    throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
	}

	final List<ISingleOperand1<? extends ISingleOperand2>> result = new ArrayList<>();

	for (final Object singleValue : (List<Object>) value) {
//	    if (singleCat == PARAM || singleCat == IPARAM) {
//		//throw new UnsupportedOperationException("Operations with params not yet supported");
//		result.addAll(getModelForArrayParam(singleCat, singleValue));
//	    } else {
		result.add(getModelForSingleOperand(singleCat, singleValue));
//	    }
	}

	return new OperandsBasedSet1(result);
    }

    protected ISingleOperand1<? extends ISingleOperand2> getModelForSingleOperand(final Pair<TokenCategory, Object> pair) {
	return getModelForSingleOperand(pair.getKey(), pair.getValue());
    }

    protected List<ISingleOperand1<? extends ISingleOperand2>> getModelForMultipleOperands(final TokenCategory cat, final Object value) {
	final List<ISingleOperand1<? extends ISingleOperand2>> result = new ArrayList<>();

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
		throw new UnsupportedOperationException("Operations with params not yet supported");
		//result.addAll(getModelForArrayParam(singleCat, singleValue));
	    } else {
		result.add(getModelForSingleOperand(singleCat, singleValue));
	    }
	}

	return result;
    }

    protected EntQueryGenerator1 getQueryBuilder() {
	return queryBuilder;
    }
}