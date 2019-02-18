package ua.com.fielden.platform.eql.stage1.builders;

import static java.lang.String.format;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.GROUPED_CONDITIONS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IPARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PROP;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.VAL;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.query.exceptions.EqlStage1ProcessingException;
import ua.com.fielden.platform.entity.query.fluent.enums.Functions;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.ConditionModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.stage1.elements.functions.CountAll1;
import ua.com.fielden.platform.eql.stage1.elements.functions.Now1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntProp1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntValue1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.elements.operands.OperandsBasedSet1;
import ua.com.fielden.platform.eql.stage1.elements.operands.QueryBasedSet1;
import ua.com.fielden.platform.eql.stage2.elements.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.elements.ISingleOperand2;
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
    private final EntQueryGenerator queryBuilder;

    protected AbstractTokensBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
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
            case COND_TOKENS: //
                tokens.add(new Pair<TokenCategory, Object>(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, (ConditionModel) value, false).getModel()));
                break;
            case NEGATED_COND_TOKENS: //
                tokens.add(new Pair<TokenCategory, Object>(GROUPED_CONDITIONS, new StandAloneConditionBuilder(queryBuilder, (ConditionModel) value, true).getModel()));
                break;
            case LOGICAL_OPERATOR:
                setChild(new CompoundConditionBuilder(this, queryBuilder, cat, value));
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

    public <V> V firstValue() {
        return tokens.size() < 1 ? null : (V) tokens.get(0).getValue();
    }

    public <V> V secondValue() {
        return tokens.size() < 2 ? null : (V) tokens.get(1).getValue();
    }

    public <V> V thirdValue() {
        return tokens.size() < 3 ? null : (V) tokens.get(2).getValue();
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
            return new EntProp1((String) value, queryBuilder.nextCondtextId());
        case EXT_PROP:
            return new EntProp1((String) value, true, queryBuilder.nextCondtextId());
//        case PARAM:
//            return new EntParam1((String) value);
//        case IPARAM:
//            return new EntParam1((String) value, true);
        case VAL:
            return new EntValue1(value);
        case IVAL:
            return new EntValue1(value, true);
        case ZERO_ARG_FUNCTION:
            return getZeroArgFunctionModel((Functions) value);
        case EXPR:
        case FUNCTION_MODEL:
            return (ISingleOperand1) value;
        case EXPR_TOKENS:
            return (ISingleOperand1) new StandAloneExpressionBuilder(queryBuilder, (ExpressionModel) value).getResult().getValue();
        case EQUERY_TOKENS:
        case ALL_OPERATOR:
        case ANY_OPERATOR:
            return queryBuilder.generateEntQueryAsSubquery((QueryModel) value);
        default:
            throw new RuntimeException("Unrecognised token category for SingleOperand: " + cat);
        }
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
            if (singleCat == PARAM || singleCat == IPARAM) {
                throw new UnsupportedOperationException("Operations with params not yet supported");
                //result.addAll(getModelForArrayParam(singleCat, singleValue));
            } else {
                result.add(getModelForSingleOperand(singleCat, singleValue));
            }
        }

        return new OperandsBasedSet1(result);
    }

    protected ISingleOperand1<? extends ISingleOperand2> getModelForSingleOperand(final Pair<TokenCategory, Object> pair) {
        return getModelForSingleOperand(pair.getKey(), pair.getValue());
    }

    protected List<ISingleOperand1<? extends ISingleOperand2>> getModelForMultipleOperands(final TokenCategory cat, final Object value) {
        final List<ISingleOperand1<? extends ISingleOperand2>> result = new ArrayList<>();

        final TokenCategory singleCat;

        switch (cat) {
        case ANY_OF_PROPS:
        case ALL_OF_PROPS:
            singleCat = PROP;
            break;
        case ANY_OF_PARAMS:
        case ALL_OF_PARAMS:
        case ANY_OF_IPARAMS:
        case ALL_OF_IPARAMS:
            throw new EqlStage1ProcessingException(format("Param related token [%s] processing should be moved to stage 3.", cat));
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
            result.add(getModelForSingleOperand(singleCat, singleValue));
        }

        return result;
    }

    protected EntQueryGenerator getQueryBuilder() {
        return queryBuilder;
    }
}