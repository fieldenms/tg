package ua.com.fielden.platform.entity.query.generation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.ComparisonTest;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.EntValue;
import ua.com.fielden.platform.entity.query.generation.elements.ExistenceTest;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditions;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.entity.query.generation.elements.ISetOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.LikeTest;
import ua.com.fielden.platform.entity.query.generation.elements.LowerCaseOf;
import ua.com.fielden.platform.entity.query.generation.elements.NullTest;
import ua.com.fielden.platform.entity.query.generation.elements.QuantifiedTest;
import ua.com.fielden.platform.entity.query.generation.elements.Quantifier;
import ua.com.fielden.platform.entity.query.generation.elements.SetTest;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.utils.Pair;

public class ConditionBuilder extends AbstractTokensBuilder {
    private final static ICondition alwaysTrue = new ComparisonTest(new EntValue(0), ComparisonOperator.EQ, new EntValue(0));

    private final static List<TokenCategory> singleOperands = Arrays.asList(new TokenCategory[] { TokenCategory.PROP, TokenCategory.EXT_PROP, TokenCategory.PARAM, TokenCategory.IPARAM, TokenCategory.VAL,
	    TokenCategory.IVAL, TokenCategory.EXPR, TokenCategory.FUNCTION_MODEL, TokenCategory.EQUERY_TOKENS, TokenCategory.EXPR_TOKENS });
    private final static List<TokenCategory> mutlipleAnyOperands = Arrays.asList(new TokenCategory[] { TokenCategory.ANY_OF_PROPS, TokenCategory.ANY_OF_PARAMS,
	    TokenCategory.ANY_OF_VALUES, TokenCategory.ANY_OF_EQUERY_TOKENS, TokenCategory.ANY_OF_EXPR_TOKENS });
    private final static List<TokenCategory> mutlipleAllOperands = Arrays.asList(new TokenCategory[] { TokenCategory.ALL_OF_PROPS, TokenCategory.ALL_OF_PARAMS,
	    TokenCategory.ALL_OF_VALUES, TokenCategory.ALL_OF_EQUERY_TOKENS, TokenCategory.ALL_OF_EXPR_TOKENS });
    private final static List<TokenCategory> mutlipleOperands = new ArrayList<TokenCategory>();
    private final static List<TokenCategory> setOperands = Arrays.asList(new TokenCategory[] { TokenCategory.SET_OF_PROPS, TokenCategory.SET_OF_PARAMS,
	    TokenCategory.SET_OF_VALUES, TokenCategory.EQUERY_TOKENS, TokenCategory.SET_OF_EXPR_TOKENS });
    private final static List<TokenCategory> quantifiers = Arrays.asList(new TokenCategory[] { TokenCategory.ANY_OPERATOR, TokenCategory.ALL_OPERATOR });
    static {
	mutlipleOperands.addAll(mutlipleAllOperands);
	mutlipleOperands.addAll(mutlipleAnyOperands);
    }

    protected ConditionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
	super(parent, queryBuilder, paramValues);
    }

    private boolean isPlainExistenceTest() {
	return getSize() == 2 && TokenCategory.EXISTS_OPERATOR.equals(firstCat()) && TokenCategory.EQUERY_TOKENS.equals(secondCat());
    }

    private boolean isMultipleExistenceTest() {
	return getSize() == 2 && TokenCategory.EXISTS_OPERATOR.equals(firstCat())
		&& (TokenCategory.ANY_OF_EQUERY_TOKENS.equals(secondCat()) || TokenCategory.ALL_OF_EQUERY_TOKENS.equals(secondCat()));
    }

    private boolean isGroupOfConditions() {
	return getSize() == 1 && TokenCategory.GROUPED_CONDITIONS.equals(firstCat());
    }

    private boolean isExternalConditions() {
	return getSize() == 1 && TokenCategory.COND_TOKENS.equals(firstCat());
    }

    private boolean isPlainNullTest() {
	return getSize() == 2 && singleOperands.contains(firstCat()) && TokenCategory.NULL_OPERATOR.equals(secondCat());
    }

    private boolean isMultipleNullTest() {
	return getSize() == 2 && mutlipleOperands.contains(firstCat()) && TokenCategory.NULL_OPERATOR.equals(secondCat());
    }

    private boolean testThreeSome(final List<TokenCategory> leftRange, final TokenCategory operator, final List<TokenCategory> rightRange) {
	return getSize() == 3 && leftRange.contains(firstCat()) && operator.equals(secondCat()) && rightRange.contains(thirdCat());
    }

    private boolean isPlainComparisonTest() {
	return testThreeSome(singleOperands, TokenCategory.COMPARISON_OPERATOR, singleOperands);
    }

    private boolean isSingleVsMultiplePlainComparisonTest() {
	return testThreeSome(singleOperands, TokenCategory.COMPARISON_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsSingleComparisonTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.COMPARISON_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsMultipleComparisonTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.COMPARISON_OPERATOR, mutlipleOperands);
    }

    private boolean isPlainLikeTest() {
	return testThreeSome(singleOperands, TokenCategory.LIKE_OPERATOR, singleOperands);
    }

    private boolean isPlainILikeTest() {
	return testThreeSome(singleOperands, TokenCategory.ILIKE_OPERATOR, singleOperands);
    }

    private boolean isSingleVsMultipleLikeTest() {
	return testThreeSome(singleOperands, TokenCategory.LIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isSingleVsMultipleILikeTest() {
	return testThreeSome(singleOperands, TokenCategory.ILIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsSingleLikeTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.LIKE_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsSingleILikeTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.ILIKE_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsMultipleLikeTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.LIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsMultipleILikeTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.ILIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isPlainSetTest() {
	return testThreeSome(singleOperands, TokenCategory.IN_OPERATOR, setOperands);
    }

    private boolean isMultipleSetTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.IN_OPERATOR, setOperands);
    }

    private boolean isPlainQuantifiedTest() {
	return testThreeSome(singleOperands, TokenCategory.COMPARISON_OPERATOR, quantifiers);
    }

    private boolean isMultipleQuantifiedTest() {
	return testThreeSome(mutlipleOperands, TokenCategory.COMPARISON_OPERATOR, quantifiers);
    }

    @Override
    public boolean isClosing() {
	return isPlainExistenceTest() || isMultipleExistenceTest() || isGroupOfConditions() || isExternalConditions() || //
		isPlainNullTest() || isMultipleNullTest() || //
		isPlainComparisonTest() || isMultipleVsSingleComparisonTest() || isMultipleVsMultipleComparisonTest() || isSingleVsMultiplePlainComparisonTest() || //
		isPlainLikeTest() || isMultipleVsSingleLikeTest() || isMultipleVsMultipleLikeTest() || isSingleVsMultipleLikeTest() || //
		isPlainILikeTest() || isMultipleVsSingleILikeTest() || isMultipleVsMultipleILikeTest() || isSingleVsMultipleILikeTest() || //
		isPlainSetTest() || isMultipleSetTest() || isPlainQuantifiedTest() || isMultipleQuantifiedTest() //
	;
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
	if (isPlainNullTest()) {
	    return handleIgnore(getPlainNullTest());
	} else if (isMultipleNullTest()) {
	    return handleIgnore(getMultipleNullTest());
	} else if (isGroupOfConditions()) {
	    return getResultForGroupOfConditions();
	} else if (isExternalConditions()) {
	    return getResultForExternalConditions();
	} else if (isPlainExistenceTest()) {
	    return handleIgnore(getPlainExistenceTest());
	} else if (isMultipleExistenceTest()) {
	    return handleIgnore(getMultipleExistenceTest());
	} else if (isPlainComparisonTest()) {
	    return handleIgnore(getPlainComparisonTest());
	} else if (isMultipleVsSingleComparisonTest()) {
	    return handleIgnore(getMultipleVsSingleComparisonTest());
	} else if (isMultipleVsMultipleComparisonTest()) {
	    return handleIgnore(getMultipleVsMultipleComparisonTest());
	} else if (isSingleVsMultiplePlainComparisonTest()) {
	    return handleIgnore(getSingleVsMultipleComparisonTest());
	} else if (isPlainLikeTest()) {
	    return handleIgnore(getPlainLikeTest());
	} else if (isMultipleVsSingleLikeTest()) {
	    return handleIgnore(getMultipleVsSingleLikeTest());
	} else if (isMultipleVsMultipleLikeTest()) {
	    return handleIgnore(getMultipleVsMultipleLikeTest());
	} else if (isSingleVsMultipleLikeTest()) {
	    return handleIgnore(getSingleVsMultipleLikeTest());
	} else if (isPlainILikeTest()) {
	    return handleIgnore(getPlainILikeTest());
	} else if (isMultipleVsSingleILikeTest()) {
	    return handleIgnore(getMultipleVsSingleILikeTest());
	} else if (isMultipleVsMultipleILikeTest()) {
	    return handleIgnore(getMultipleVsMultipleILikeTest());
	} else if (isSingleVsMultipleILikeTest()) {
	    return handleIgnore(getSingleVsMultipleILikeTest());
	} else if (isPlainSetTest()) {
	    return handleIgnore(getPlainSetTest());
	} else if (isMultipleSetTest()) {
	    return handleIgnore(getMultipleSetTest());
	} else if (isPlainQuantifiedTest()) {
	    return handleIgnore(getPlainQuantifiedTest());
	} else if (isMultipleQuantifiedTest()) {
	    return handleIgnore(getMultipleQuantifiedTest());
	} else {
	    throw new RuntimeException("Unrecognised result");
	}
    }

    private Pair<TokenCategory, Object> handleIgnore(final ICondition condition) {
	if (condition.ignore()) {
	    return getAlwaysTrueCondition();
	} else {
	    return new Pair<TokenCategory, Object>(TokenCategory.CONDITION, condition);
	}
    }

    private ICondition getPlainQuantifiedTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final EntQuery secondOperand = (EntQuery) getModelForSingleOperand(thirdCat(), thirdValue());
	final Quantifier quantifier = TokenCategory.ANY_OPERATOR.equals(thirdCat()) ? Quantifier.ANY : Quantifier.ALL;
	return new QuantifiedTest(firstOperand, (ComparisonOperator) secondValue(), quantifier, secondOperand);
    }

    private GroupedConditions getMultipleQuantifiedTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final EntQuery secondOperand = (EntQuery) getModelForSingleOperand(thirdCat(), thirdValue());
	final Quantifier quantifier = TokenCategory.ANY_OPERATOR.equals(thirdCat()) ? Quantifier.ANY : Quantifier.ALL;
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new QuantifiedTest(operand, (ComparisonOperator) secondValue(), quantifier, secondOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getGroup(final List<ICondition> conditions, final LogicalOperator logicalOperator) {
	final Iterator<ICondition> iterator = conditions.iterator();
	if (!iterator.hasNext()) {
	    return new GroupedConditions(false, (ICondition) getAlwaysTrueCondition().getValue(), new ArrayList<CompoundCondition>());
	} else {
	    final ICondition firstCondition = iterator.next();
	    final List<CompoundCondition> otherConditions = new ArrayList<CompoundCondition>();
	    for (; iterator.hasNext();) {
		final CompoundCondition subsequentCompoundCondition = new CompoundCondition(logicalOperator, iterator.next());
		otherConditions.add(subsequentCompoundCondition);
	    }
	    return new GroupedConditions(false, firstCondition, otherConditions);
	}
    }

    private Pair<TokenCategory, Object> getResultForGroupOfConditions() {
	return getTokens().get(0);
    }

    private Pair<TokenCategory, Object> getResultForExternalConditions() {
	return getTokens().get(0);
    }

    private NullTest getPlainNullTest() {
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	return new NullTest(operand, (Boolean) secondValue());
    }

    private GroupedConditions getMultipleNullTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new NullTest(operand, (Boolean) secondValue()));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getMultipleVsMultipleComparisonTest() {
	final List<ISingleOperand> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<ISingleOperand> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

	final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;

	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition> outerConditions = new ArrayList<ICondition>();
	for (final ISingleOperand leftOperand : leftOperands) {
	    final List<ICondition> innerConditions = new ArrayList<ICondition>();
	    for (final ISingleOperand rightOperand : rightOperands) {
		innerConditions.add(new ComparisonTest(leftOperand, operator, rightOperand));
	    }
	    final GroupedConditions group = getGroup(innerConditions, rightLogicalOperator);
	    outerConditions.add(group);
	}
	return getGroup(outerConditions, leftLogicalOperator);
    }

    private GroupedConditions getMultipleVsSingleComparisonTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new ComparisonTest(operand, operator, singleOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getSingleVsMultipleComparisonTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new ComparisonTest(singleOperand, operator, operand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private ComparisonTest getPlainComparisonTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISingleOperand secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	return new ComparisonTest(firstOperand, (ComparisonOperator) secondValue(), secondOperand);
    }

    private LikeTest getPlainLikeTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISingleOperand secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	return new LikeTest(firstOperand, secondOperand, (Boolean) secondValue(), false);
    }

    private LikeTest getPlainILikeTest() {
	final ISingleOperand firstOperand = new LowerCaseOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
	final ISingleOperand secondOperand = new LowerCaseOf(getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
	return new LikeTest(firstOperand, secondOperand, (Boolean) secondValue(), false);
    }

    private GroupedConditions getMultipleVsMultipleLikeTest() {
	final List<ISingleOperand> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<ISingleOperand> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

	final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;

	final List<ICondition> outerConditions = new ArrayList<ICondition>();
	for (final ISingleOperand leftOperand : leftOperands) {
	    final List<ICondition> innerConditions = new ArrayList<ICondition>();
	    for (final ISingleOperand rightOperand : rightOperands) {
		innerConditions.add(new LikeTest(leftOperand, rightOperand, (Boolean) secondValue(), false));
	    }
	    final GroupedConditions group = getGroup(innerConditions, rightLogicalOperator);
	    outerConditions.add(group);
	}
	return getGroup(outerConditions, leftLogicalOperator);
    }

    private GroupedConditions getMultipleVsMultipleILikeTest() {
	final List<ISingleOperand> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<ISingleOperand> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

	final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;

	final List<ICondition> outerConditions = new ArrayList<ICondition>();
	for (final ISingleOperand leftOperand : leftOperands) {
	    final List<ICondition> innerConditions = new ArrayList<ICondition>();
	    for (final ISingleOperand rightOperand : rightOperands) {
		innerConditions.add(new LikeTest(new LowerCaseOf(leftOperand, getDbVersion()), new LowerCaseOf(rightOperand, getDbVersion()), (Boolean) secondValue(), false));
	    }
	    final GroupedConditions group = getGroup(innerConditions, rightLogicalOperator);
	    outerConditions.add(group);
	}
	return getGroup(outerConditions, leftLogicalOperator);
    }

    private GroupedConditions getMultipleVsSingleLikeTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new LikeTest(operand, singleOperand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getMultipleVsSingleILikeTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand singleOperand = new LowerCaseOf(getModelForSingleOperand(thirdCat(), thirdValue()), getDbVersion());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new LikeTest(new LowerCaseOf(operand, getDbVersion()), singleOperand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }


    private GroupedConditions getSingleVsMultipleLikeTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(firstCat(), firstValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new LikeTest(singleOperand, operand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getSingleVsMultipleILikeTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand singleOperand = new LowerCaseOf(getModelForSingleOperand(firstCat(), firstValue()), getDbVersion());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new LikeTest(singleOperand, new LowerCaseOf(operand, getDbVersion()), (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private ExistenceTest getPlainExistenceTest() {
	return new ExistenceTest((Boolean) firstValue(), getQueryBuilder().generateEntQueryAsSubquery((QueryModel) secondValue(), getParamValues()));
    }

    private SetTest getPlainSetTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISetOperand setOperand = getModelForSetOperand(thirdCat(), thirdValue());
	return new SetTest(firstOperand, (Boolean) secondValue(), setOperand);
    }

    private GroupedConditions getMultipleSetTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISetOperand setOperand = getModelForSetOperand(thirdCat(), thirdValue());

	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new SetTest(operand, (Boolean) secondValue(), setOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getMultipleExistenceTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(secondCat(), secondValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new ExistenceTest((Boolean) firstValue(), (EntQuery) operand));
	}
	final LogicalOperator logicalOperator = TokenCategory.ANY_OF_EQUERY_TOKENS.equals(secondCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return getGroup(conditions, logicalOperator);
    }

    private Pair<TokenCategory, Object> getAlwaysTrueCondition() {
	return new Pair<TokenCategory, Object>(TokenCategory.CONDITION, alwaysTrue);
    }
}