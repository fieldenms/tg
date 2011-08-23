package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.elements.EntParam;
import ua.com.fielden.platform.entity.query.model.elements.EntProp;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntSet;
import ua.com.fielden.platform.entity.query.model.elements.EntSetFromQryModel;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;
import ua.com.fielden.platform.entity.query.model.elements.Functions;
import ua.com.fielden.platform.entity.query.model.elements.ISetOperand;
import ua.com.fielden.platform.entity.query.model.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
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
    private final DbVersion dbVersion;
    private final QueryBuilder queryBuilder;

    protected AbstractTokensBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion) {
	this.parent = parent;
	this.dbVersion = dbVersion;
	this.queryBuilder = new QueryBuilder(dbVersion);
    }

    private void add (final Functions function) {
	switch (function) {
	case SUM:
	    setChild(new SumOfBuilder(this, dbVersion));
	    break;
	case COUNT:
	    setChild(new CountOfBuilder(this, dbVersion));
	    break;
	case AVERAGE:
	    setChild(new AverageOfBuilder(this, dbVersion));
	    break;
	case MIN:
	    setChild(new MinOfBuilder(this, dbVersion));
	    break;
	case MAX:
	    setChild(new MaxOfBuilder(this, dbVersion));
	    break;
	case DAY:
	    setChild(new DayOfBuilder(this, dbVersion));
	    break;
	case MONTH:
	    setChild(new MonthOfBuilder(this, dbVersion));
	    break;
	default:
	    // TODO implement the rest
	    throw new RuntimeException("Unrecognised function token: " + function);
	}
    }

    public void add(final TokenCategory cat, final Object value) {
	if (child != null) {
	    child.add(cat, value);
	} else {
	    switch (cat) {
	    case BEGIN_EXPR: //eats token
		setChild(new ExpressionBuilder(this, dbVersion));
		break;
	    case FUNCTION: //eats token
		add((Functions) value);
		break;
	    case BEGIN_COND: //eats token
		setChild(new GroupedConditionsBuilder(this, dbVersion, (Boolean) value));
		break;
	    case LOGICAL_OPERATOR:
		setChild(new CompoundConditionBuilder(this, dbVersion, cat, value));
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

    protected ISingleOperand getModelForSingleOperand(final TokenCategory cat, final Object value) {
	switch (cat) {
	case PROP:
	    return new EntProp((String) value);
	case PARAM:
	    return new EntParam((String) value);
	case VAL:
	    return new EntValue(value);
	case EXPR:
	case FUNCTION_MODEL:
	    return (ISingleOperand) value;
	case EXPR_TOKENS:
	    return (ISingleOperand) new StandAloneExpressionBuilder(dbVersion, (ExpressionModel) value).getResult().getValue();
	case EQUERY_TOKENS:
	case ALL_OPERATOR:
	case ANY_OPERATOR:
	    return queryBuilder.getQry((QueryModel) value);
	default:
	    throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
	}
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
	    return new EntSetFromQryModel((EntQuery) getModelForSingleOperand(cat, value));
	default:
	    throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
	}

	final List<ISingleOperand> result = new ArrayList<ISingleOperand>();

	for (final Object singleValue : (List<Object>) value) {
	    result.add(getModelForSingleOperand(singleCat, singleValue));
	}

	return new EntSet(result);
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

    public DbVersion getDbVersion() {
        return dbVersion;
    }

    protected QueryBuilder getQueryBuilder() {
        return queryBuilder;
    }
}