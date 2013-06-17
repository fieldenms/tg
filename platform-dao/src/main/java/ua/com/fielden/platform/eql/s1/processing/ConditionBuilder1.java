package ua.com.fielden.platform.eql.s1.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.meta.Quantifier;
import ua.com.fielden.platform.eql.s1.elements.ComparisonTest1;
import ua.com.fielden.platform.eql.s1.elements.CompoundCondition1;
import ua.com.fielden.platform.eql.s1.elements.Conditions1;
import ua.com.fielden.platform.eql.s1.elements.EntQuery1;
import ua.com.fielden.platform.eql.s1.elements.ExistenceTest1;
import ua.com.fielden.platform.eql.s1.elements.ICondition1;
import ua.com.fielden.platform.eql.s1.elements.ISetOperand1;
import ua.com.fielden.platform.eql.s1.elements.ISingleOperand1;
import ua.com.fielden.platform.eql.s1.elements.LikeTest1;
import ua.com.fielden.platform.eql.s1.elements.LowerCaseOf1;
import ua.com.fielden.platform.eql.s1.elements.NullTest1;
import ua.com.fielden.platform.eql.s1.elements.QuantifiedTest1;
import ua.com.fielden.platform.eql.s1.elements.SetTest1;
import ua.com.fielden.platform.eql.s2.elements.ICondition2;
import ua.com.fielden.platform.eql.s2.elements.ISetOperand2;
import ua.com.fielden.platform.eql.s2.elements.ISingleOperand2;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.entity.query.fluent.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.LogicalOperator.OR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ALL_OF_EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ALL_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ALL_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ALL_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ALL_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ALL_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ALL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ANY_OF_EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ANY_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ANY_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ANY_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ANY_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ANY_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ANY_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.COMPARISON_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.CONDITION;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.EXISTS_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.EXPR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.EXT_PROP;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.FUNCTION_MODEL;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.GROUPED_CONDITIONS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.ILIKE_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.IN_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.IPARAM;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.IVAL;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.LIKE_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.NULL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.PARAM;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.PROP;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.SET_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.SET_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.SET_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.SET_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.SET_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.TokenCategory.VAL;
import static ua.com.fielden.platform.eql.meta.Quantifier.ALL;
import static ua.com.fielden.platform.eql.meta.Quantifier.ANY;

public class ConditionBuilder1 extends AbstractTokensBuilder1 {

    private final static List<TokenCategory> singleOperands = Arrays.asList(new TokenCategory[] { //
	    PROP, EXT_PROP, PARAM, IPARAM, //
	    VAL, IVAL, EXPR, FUNCTION_MODEL, EQUERY_TOKENS, EXPR_TOKENS });
    private final static List<TokenCategory> mutlipleAnyOperands = Arrays.asList(new TokenCategory[] { //
	    ANY_OF_PROPS, ANY_OF_PARAMS, ANY_OF_IPARAMS, //
	    ANY_OF_VALUES, ANY_OF_EQUERY_TOKENS, ANY_OF_EXPR_TOKENS });
    private final static List<TokenCategory> mutlipleAllOperands = Arrays.asList(new TokenCategory[] { //
	    ALL_OF_PROPS, ALL_OF_PARAMS, ALL_OF_IPARAMS, //
	    ALL_OF_VALUES, ALL_OF_EQUERY_TOKENS, ALL_OF_EXPR_TOKENS });
    private final static List<TokenCategory> setOperands = Arrays.asList(new TokenCategory[] { //
	    SET_OF_PROPS, SET_OF_PARAMS, SET_OF_IPARAMS, //
	    SET_OF_VALUES, EQUERY_TOKENS, SET_OF_EXPR_TOKENS });
    private final static List<TokenCategory> quantifiers = Arrays.asList(new TokenCategory[] { //
	    ANY_OPERATOR, ALL_OPERATOR });
    private final static List<TokenCategory> mutlipleOperands = new ArrayList<TokenCategory>();
    static {
	mutlipleOperands.addAll(mutlipleAllOperands);
	mutlipleOperands.addAll(mutlipleAnyOperands);
    }

    protected ConditionBuilder1(final AbstractTokensBuilder1 parent, final EntQueryGenerator1 queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    private boolean isPlainExistenceTest() {
	return getSize() == 2 && EXISTS_OPERATOR ==firstCat() && EQUERY_TOKENS == secondCat();
    }

    private boolean isMultipleExistenceTest() {
	return getSize() == 2 && EXISTS_OPERATOR == firstCat()
		&& (ANY_OF_EQUERY_TOKENS == secondCat() || ALL_OF_EQUERY_TOKENS == secondCat());
    }

    private boolean isGroupOfConditions() {
	return getSize() == 1 && GROUPED_CONDITIONS == firstCat();
    }

    private boolean isPlainNullTest() {
	return getSize() == 2 && singleOperands.contains(firstCat()) && NULL_OPERATOR == secondCat();
    }

    private boolean isMultipleNullTest() {
	return getSize() == 2 && mutlipleOperands.contains(firstCat()) && NULL_OPERATOR == secondCat();
    }

    private boolean testThreeSome(final List<TokenCategory> leftRange, final TokenCategory operator, final List<TokenCategory> rightRange) {
	return getSize() == 3 && leftRange.contains(firstCat()) && operator == secondCat() && rightRange.contains(thirdCat());
    }

    private boolean isPlainComparisonTest() {
	return testThreeSome(singleOperands, COMPARISON_OPERATOR, singleOperands);
    }

    private boolean isSingleVsMultiplePlainComparisonTest() {
	return testThreeSome(singleOperands, COMPARISON_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsSingleComparisonTest() {
	return testThreeSome(mutlipleOperands, COMPARISON_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsMultipleComparisonTest() {
	return testThreeSome(mutlipleOperands, COMPARISON_OPERATOR, mutlipleOperands);
    }

    private boolean isPlainLikeTest() {
	return testThreeSome(singleOperands, LIKE_OPERATOR, singleOperands);
    }

    private boolean isPlainILikeTest() {
	return testThreeSome(singleOperands, ILIKE_OPERATOR, singleOperands);
    }

    private boolean isSingleVsMultipleLikeTest() {
	return testThreeSome(singleOperands, LIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isSingleVsMultipleILikeTest() {
	return testThreeSome(singleOperands, ILIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsSingleLikeTest() {
	return testThreeSome(mutlipleOperands, LIKE_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsSingleILikeTest() {
	return testThreeSome(mutlipleOperands, ILIKE_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsMultipleLikeTest() {
	return testThreeSome(mutlipleOperands, LIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsMultipleILikeTest() {
	return testThreeSome(mutlipleOperands, ILIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isPlainSetTest() {
	return testThreeSome(singleOperands, IN_OPERATOR, setOperands);
    }

    private boolean isMultipleSetTest() {
	return testThreeSome(mutlipleOperands, IN_OPERATOR, setOperands);
    }

    private boolean isPlainQuantifiedTest() {
	return testThreeSome(singleOperands, COMPARISON_OPERATOR, quantifiers);
    }

    private boolean isMultipleQuantifiedTest() {
	return testThreeSome(mutlipleOperands, COMPARISON_OPERATOR, quantifiers);
    }

    @Override
    public boolean isClosing() {
	return isPlainExistenceTest() || isMultipleExistenceTest() || isGroupOfConditions() || //
		isPlainNullTest() || isMultipleNullTest() || //
		isPlainComparisonTest() || isMultipleVsSingleComparisonTest() || isMultipleVsMultipleComparisonTest() || isSingleVsMultiplePlainComparisonTest() || //
		isPlainLikeTest() || isMultipleVsSingleLikeTest() || isMultipleVsMultipleLikeTest() || isSingleVsMultipleLikeTest() || //
		isPlainILikeTest() || isMultipleVsSingleILikeTest() || isMultipleVsMultipleILikeTest() || isSingleVsMultipleILikeTest() || //
		isPlainSetTest() || isMultipleSetTest() || isPlainQuantifiedTest() || isMultipleQuantifiedTest() //
	;
    }

    private Object getResultantCondition() {
	if (isPlainNullTest()) { return getPlainNullTest();
	} else if (isMultipleNullTest()) { return getMultipleNullTest();
	} else if (isGroupOfConditions()) { return getTokens().get(0).getValue();
	} else if (isPlainExistenceTest()) { return getPlainExistenceTest();
	} else if (isMultipleExistenceTest()) { return getMultipleExistenceTest();
	} else if (isPlainComparisonTest()) { return getPlainComparisonTest();
	} else if (isMultipleVsSingleComparisonTest()) { return getMultipleVsSingleComparisonTest();
	} else if (isMultipleVsMultipleComparisonTest()) { return getMultipleVsMultipleComparisonTest();
	} else if (isSingleVsMultiplePlainComparisonTest()) { return getSingleVsMultipleComparisonTest();
	} else if (isPlainLikeTest()) { return getPlainLikeTest();
	} else if (isMultipleVsSingleLikeTest()) { return getMultipleVsSingleLikeTest();
	} else if (isMultipleVsMultipleLikeTest()) { return getMultipleVsMultipleLikeTest();
	} else if (isSingleVsMultipleLikeTest()) { return getSingleVsMultipleLikeTest();
	} else if (isPlainILikeTest()) { return getPlainILikeTest();
	} else if (isMultipleVsSingleILikeTest()) { return getMultipleVsSingleILikeTest();
	} else if (isMultipleVsMultipleILikeTest()) { return getMultipleVsMultipleILikeTest();
	} else if (isSingleVsMultipleILikeTest()) { return getSingleVsMultipleILikeTest();
	} else if (isPlainSetTest()) { return getPlainSetTest();
	} else if (isMultipleSetTest()) { return getMultipleSetTest();
	} else if (isPlainQuantifiedTest()) { return getPlainQuantifiedTest();
	} else if (isMultipleQuantifiedTest()) { return getMultipleQuantifiedTest();
	} else {
	    throw new RuntimeException("Unrecognised result");
	}
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	return new Pair<TokenCategory, Object>(CONDITION, getResultantCondition());
    }

    private ICondition1<? extends ICondition2> getPlainQuantifiedTest() {
	final ISingleOperand1<? extends ISingleOperand2> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final EntQuery1 secondOperand = (EntQuery1) getModelForSingleOperand(thirdCat(), thirdValue());
	final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
	return new QuantifiedTest1(firstOperand, (ComparisonOperator) secondValue(), quantifier, secondOperand);
    }

    private Conditions1 getMultipleQuantifiedTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final EntQuery1 secondOperand = (EntQuery1) getModelForSingleOperand(thirdCat(), thirdValue());
	final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new QuantifiedTest1(operand, (ComparisonOperator) secondValue(), quantifier, secondOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getGroup(final List<ICondition1<? extends ICondition2>> conditions, final LogicalOperator logicalOperator) {
	final Iterator<ICondition1<? extends ICondition2>> iterator = conditions.iterator();
	if (!iterator.hasNext()) {
	    return new Conditions1();
	} else {
	    final ICondition1<? extends ICondition2> firstCondition = iterator.next();
	    final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();
	    for (; iterator.hasNext();) {
		final CompoundCondition1 subsequentCompoundCondition = new CompoundCondition1(logicalOperator, iterator.next());
		otherConditions.add(subsequentCompoundCondition);
	    }
	    return new Conditions1(false, firstCondition, otherConditions);
	}
    }

    private NullTest1 getPlainNullTest() {
	final ISingleOperand1<? extends ISingleOperand2> operand = getModelForSingleOperand(firstCat(), firstValue());
	return new NullTest1(operand, (Boolean) secondValue());
    }

    private Conditions1 getMultipleNullTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new NullTest1(operand, (Boolean) secondValue()));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getMultipleVsMultipleComparisonTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

	final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition1<? extends ICondition2>> outerConditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> leftOperand : leftOperands) {
	    final List<ICondition1<? extends ICondition2>> innerConditions = new ArrayList<>();
	    for (final ISingleOperand1<? extends ISingleOperand2> rightOperand : rightOperands) {
		innerConditions.add(new ComparisonTest1(leftOperand, operator, rightOperand));
	    }
	    final Conditions1 group = getGroup(innerConditions, rightLogicalOperator);
	    outerConditions.add(group);
	}
	return getGroup(outerConditions, leftLogicalOperator);
    }

    private Conditions1 getMultipleVsSingleComparisonTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand1<? extends ISingleOperand2> singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new ComparisonTest1(operand, operator, singleOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getSingleVsMultipleComparisonTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand1<? extends ISingleOperand2> singleOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new ComparisonTest1(singleOperand, operator, operand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private ComparisonTest1 getPlainComparisonTest() {
	final ISingleOperand1<? extends ISingleOperand2> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISingleOperand1<? extends ISingleOperand2> secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	return new ComparisonTest1(firstOperand, (ComparisonOperator) secondValue(), secondOperand);
    }

    private LikeTest1 getPlainLikeTest() {
	final ISingleOperand1<? extends ISingleOperand2> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISingleOperand1<? extends ISingleOperand2> secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	return new LikeTest1(firstOperand, secondOperand, (Boolean) secondValue(), false);
    }

    private LikeTest1 getPlainILikeTest() {
	final ISingleOperand1<? extends ISingleOperand2> firstOperand = new LowerCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
	final ISingleOperand1<? extends ISingleOperand2> secondOperand = new LowerCaseOf1(getModelForSingleOperand(thirdCat(), thirdValue()));
	return new LikeTest1(firstOperand, secondOperand, (Boolean) secondValue(), false);
    }

    private Conditions1 getMultipleVsMultipleLikeTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

	final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

	final List<ICondition1<? extends ICondition2>> outerConditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> leftOperand : leftOperands) {
	    final List<ICondition1<? extends ICondition2>> innerConditions = new ArrayList<>();
	    for (final ISingleOperand1<? extends ISingleOperand2> rightOperand : rightOperands) {
		innerConditions.add(new LikeTest1(leftOperand, rightOperand, (Boolean) secondValue(), false));
	    }
	    final Conditions1 group = getGroup(innerConditions, rightLogicalOperator);
	    outerConditions.add(group);
	}
	return getGroup(outerConditions, leftLogicalOperator);
    }

    private Conditions1 getMultipleVsMultipleILikeTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

	final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

	final List<ICondition1<? extends ICondition2>> outerConditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> leftOperand : leftOperands) {
	    final List<ICondition1<? extends ICondition2>> innerConditions = new ArrayList<>();
	    for (final ISingleOperand1<? extends ISingleOperand2> rightOperand : rightOperands) {
		innerConditions.add(new LikeTest1(new LowerCaseOf1(leftOperand), new LowerCaseOf1(rightOperand), (Boolean) secondValue(), false));
	    }
	    final Conditions1 group = getGroup(innerConditions, rightLogicalOperator);
	    outerConditions.add(group);
	}
	return getGroup(outerConditions, leftLogicalOperator);
    }

    private Conditions1 getMultipleVsSingleLikeTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand1<? extends ISingleOperand2> singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new LikeTest1(operand, singleOperand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getMultipleVsSingleILikeTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand1<? extends ISingleOperand2> singleOperand = new LowerCaseOf1(getModelForSingleOperand(thirdCat(), thirdValue()));
	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new LikeTest1(new LowerCaseOf1(operand), singleOperand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }


    private Conditions1 getSingleVsMultipleLikeTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand1<? extends ISingleOperand2> singleOperand = getModelForSingleOperand(firstCat(), firstValue());
	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new LikeTest1(singleOperand, operand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getSingleVsMultipleILikeTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand1<? extends ISingleOperand2> singleOperand = new LowerCaseOf1(getModelForSingleOperand(firstCat(), firstValue()));
	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new LikeTest1(singleOperand, new LowerCaseOf1(operand), (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private ExistenceTest1 getPlainExistenceTest() {
	return new ExistenceTest1((Boolean) firstValue(), getQueryBuilder().generateEntQueryAsSubquery((QueryModel) secondValue(), getParamValues()));
    }

    private SetTest1 getPlainSetTest() {
	final ISingleOperand1<? extends ISingleOperand2> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISetOperand1<? extends ISetOperand2> setOperand = getModelForSetOperand(thirdCat(), thirdValue());
	return new SetTest1(firstOperand, (Boolean) secondValue(), setOperand);
    }

    private Conditions1 getMultipleSetTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISetOperand1<? extends ISetOperand2> setOperand = getModelForSetOperand(thirdCat(), thirdValue());

	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new SetTest1(operand, (Boolean) secondValue(), setOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getMultipleExistenceTest() {
	final List<? extends ISingleOperand1<? extends ISingleOperand2>> operands = getModelForMultipleOperands(secondCat(), secondValue());
	final List<ICondition1<? extends ICondition2>> conditions = new ArrayList<>();
	for (final ISingleOperand1<? extends ISingleOperand2> operand : operands) {
	    conditions.add(new ExistenceTest1((Boolean) firstValue(), (EntQuery1) operand));
	}
	final LogicalOperator logicalOperator = ANY_OF_EQUERY_TOKENS == secondCat() ? OR : AND;
	return getGroup(conditions, logicalOperator);
    }
}