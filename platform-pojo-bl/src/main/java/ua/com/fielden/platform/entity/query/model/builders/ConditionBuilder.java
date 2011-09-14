package ua.com.fielden.platform.entity.query.model.builders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonOperator;
import ua.com.fielden.platform.entity.query.model.elements.ComparisonTestModel;
import ua.com.fielden.platform.entity.query.model.elements.CompoundConditionModel;
import ua.com.fielden.platform.entity.query.model.elements.EntQuery;
import ua.com.fielden.platform.entity.query.model.elements.EntValue;
import ua.com.fielden.platform.entity.query.model.elements.ExistenceTestModel;
import ua.com.fielden.platform.entity.query.model.elements.GroupedConditionsModel;
import ua.com.fielden.platform.entity.query.model.elements.ICondition;
import ua.com.fielden.platform.entity.query.model.elements.ISetOperand;
import ua.com.fielden.platform.entity.query.model.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.model.elements.LikeTestModel;
import ua.com.fielden.platform.entity.query.model.elements.LogicalOperator;
import ua.com.fielden.platform.entity.query.model.elements.NullTestModel;
import ua.com.fielden.platform.entity.query.model.elements.QuantifiedTestModel;
import ua.com.fielden.platform.entity.query.model.elements.Quantifier;
import ua.com.fielden.platform.entity.query.model.elements.SetTestModel;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

public class ConditionBuilder extends AbstractTokensBuilder {
    private final static List<TokenCategory> singleOperands = Arrays.asList(new TokenCategory[] {TokenCategory.PROP, TokenCategory.PARAM, TokenCategory.VAL, TokenCategory.EXPR, TokenCategory.FUNCTION_MODEL, TokenCategory.EQUERY_TOKENS, TokenCategory.EXPR_TOKENS});
    private final static List<TokenCategory> mutlipleAnyOperands = Arrays.asList(new TokenCategory[] {TokenCategory.ANY_OF_PROPS, TokenCategory.ANY_OF_PARAMS, TokenCategory.ANY_OF_VALUES, TokenCategory.ANY_OF_EQUERY_TOKENS, TokenCategory.ANY_OF_EXPR_TOKENS});
    private final static List<TokenCategory> mutlipleAllOperands = Arrays.asList(new TokenCategory[] {TokenCategory.ALL_OF_PROPS, TokenCategory.ALL_OF_PARAMS, TokenCategory.ALL_OF_VALUES, TokenCategory.ALL_OF_EQUERY_TOKENS, TokenCategory.ALL_OF_EXPR_TOKENS});
    private final static List<TokenCategory> mutlipleOperands = new ArrayList<TokenCategory>();
    private final static List<TokenCategory> setOperands = Arrays.asList(new TokenCategory[] {TokenCategory.SET_OF_PROPS, TokenCategory.SET_OF_PARAMS, TokenCategory.SET_OF_VALUES, TokenCategory.EQUERY_TOKENS, TokenCategory.SET_OF_EXPR_TOKENS});
    private final static List<TokenCategory> quantifiers = Arrays.asList(new TokenCategory[] {TokenCategory.ANY_OPERATOR, TokenCategory.ALL_OPERATOR});
    static {
	mutlipleOperands.addAll(mutlipleAllOperands);
	mutlipleOperands.addAll(mutlipleAnyOperands);
    }

    protected ConditionBuilder(final AbstractTokensBuilder parent, final DbVersion dbVersion) {
	super(parent, dbVersion);
    }

    private boolean isPlainExistenceTest() {
	return getSize() == 2 && TokenCategory.EXISTS_OPERATOR.equals(firstCat()) && TokenCategory.EQUERY_TOKENS.equals(secondCat());
    }

    private boolean isMultipleExistenceTest() {
	return getSize() == 2 && TokenCategory.EXISTS_OPERATOR.equals(firstCat()) && (TokenCategory.ANY_OF_EQUERY_TOKENS.equals(secondCat()) || TokenCategory.ALL_OF_EQUERY_TOKENS.equals(secondCat()));
    }

    private boolean isGroupOfConditions() {
	return getSize() == 1 && TokenCategory.GROUPED_CONDITIONS.equals(firstCat());
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
	return isPlainExistenceTest() || isMultipleExistenceTest() || isGroupOfConditions() || //
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
	    return getResultForPlainNullTest();
	} else if (isMultipleNullTest()) {
	    return getResultForMultipleNullTest();
	} else if (isGroupOfConditions()) {
	    return getResultForGroupOfConditions();
	} else if (isPlainExistenceTest()) {
	    return getResultForPlainExistenceTest();
	} else if (isMultipleExistenceTest()) {
	    return getResultForMultipleExistenceTest();
	} else if (isPlainComparisonTest()) {
	    return getResultForPlainComparisonTest();
	} else if (isMultipleVsSingleComparisonTest()) {
	    return getResultForMultipleVsSingleComparisonTest();
	} else if (isMultipleVsMultipleComparisonTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isSingleVsMultiplePlainComparisonTest()) {
	    return getResultForSingleVsMultipleComparisonTest();
	} else if (isPlainLikeTest()) {
	    return  getResultForPlainLikeTest();
	} else if (isMultipleVsSingleLikeTest()) {
	    return getResultForMultipleVsSingleLikeTest();
	} else if (isMultipleVsMultipleLikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isSingleVsMultipleLikeTest()) {
	    return getResultForSingleVsMultipleLikeTest();
	} else if (isPlainILikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isMultipleVsSingleILikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isMultipleVsMultipleILikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isSingleVsMultipleILikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isPlainSetTest()) {
	    return getResultForPlainSetTest();
	} else if (isMultipleSetTest()) {
	    return getResultForMultipleSetTest();
	} else if (isPlainQuantifiedTest()) {
	    return getResultForPlainQuantifiedTest();
	} else if (isMultipleQuantifiedTest()) {
	    return getResultForMultipleQuantifiedTest();
	} else {
	    throw new RuntimeException("Unrecognised result");
	}
    }

    private Pair<TokenCategory, Object> getResultForPlainQuantifiedTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final EntQuery secondOperand = (EntQuery) getModelForSingleOperand(thirdCat(), thirdValue());
	final Quantifier quantifier = TokenCategory.ANY_OPERATOR.equals(thirdCat()) ? Quantifier.ANY : Quantifier.ALL;
	return new Pair<TokenCategory, Object>(TokenCategory.QUANTIFIED_TEST, new QuantifiedTestModel(firstOperand, (ComparisonOperator) secondValue(), quantifier, secondOperand));
    }

    private Pair<TokenCategory, Object> getResultForMultipleQuantifiedTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final EntQuery secondOperand = (EntQuery) getModelForSingleOperand(thirdCat(), thirdValue());
	final Quantifier quantifier = TokenCategory.ANY_OPERATOR.equals(thirdCat()) ? Quantifier.ANY : Quantifier.ALL;
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new QuantifiedTestModel(operand, (ComparisonOperator) secondValue(), quantifier, secondOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private GroupedConditionsModel getGroup(final List<ICondition> conditions, final LogicalOperator logicalOperator) {
	final Iterator<ICondition> iterator = conditions.iterator();
	if (!iterator.hasNext()) {
	    final ICondition firstCondition = new ComparisonTestModel(new EntValue(1), ComparisonOperator.EQ, new EntValue(1));
	    return new GroupedConditionsModel(false, firstCondition, new ArrayList<CompoundConditionModel>());
	} else {
	    final ICondition firstCondition = iterator.next();
	    final List<CompoundConditionModel> otherConditions = new ArrayList<CompoundConditionModel>();
	    for (; iterator.hasNext();) {
		final CompoundConditionModel subsequentCompoundCondition = new CompoundConditionModel(logicalOperator, iterator.next());
		otherConditions.add(subsequentCompoundCondition);
	    }
	    return new GroupedConditionsModel(false, firstCondition, otherConditions);
	}
    }

    private Pair<TokenCategory, Object> getResultForGroupOfConditions() {
	return getTokens().get(0);
    }

    private Pair<TokenCategory, Object> getResultForPlainNullTest() {
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	return new Pair<TokenCategory, Object>(TokenCategory.NULL_TEST, new NullTestModel(operand, (Boolean) secondValue()));
    }

    private Pair<TokenCategory, Object> getResultForMultipleNullTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new NullTestModel(operand, (Boolean) secondValue()));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private Pair<TokenCategory, Object> getResultForMultipleVsSingleComparisonTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new ComparisonTestModel(operand, operator, singleOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private Pair<TokenCategory, Object> getResultForSingleVsMultipleComparisonTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ComparisonOperator operator = (ComparisonOperator) secondValue();

	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new ComparisonTestModel(singleOperand, operator, operand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private Pair<TokenCategory, Object> getResultForPlainComparisonTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISingleOperand secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	return new Pair<TokenCategory, Object>(TokenCategory.COMPARISON_TEST, new ComparisonTestModel(firstOperand, (ComparisonOperator) secondValue(), secondOperand));
    }

    private Pair<TokenCategory, Object> getResultForPlainLikeTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISingleOperand secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	return new Pair<TokenCategory, Object>(TokenCategory.LIKE_TEST, new LikeTestModel(firstOperand, secondOperand, (Boolean) secondValue(), false));
    }

    private Pair<TokenCategory, Object> getResultForMultipleVsSingleLikeTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new LikeTestModel(operand, singleOperand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private Pair<TokenCategory, Object> getResultForSingleVsMultipleLikeTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
	final ISingleOperand singleOperand = getModelForSingleOperand(firstCat(), firstValue());

	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new LikeTestModel(singleOperand, operand, (Boolean) secondValue(), false));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private Pair<TokenCategory, Object> getResultForPlainExistenceTest() {
	return new Pair<TokenCategory, Object>(TokenCategory.EXISTENCE_TEST, new ExistenceTestModel((Boolean) firstValue(), getQueryBuilder().generateEntQuery((QueryModel) secondValue())));
    }

    private Pair<TokenCategory, Object> getResultForPlainSetTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISetOperand setOperand = getModelForSetOperand(thirdCat(), thirdValue());
	return new Pair<TokenCategory, Object>(TokenCategory.SET_TEST, new SetTestModel(firstOperand, (Boolean) secondValue(), setOperand));
    }

    private Pair<TokenCategory, Object> getResultForMultipleSetTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final ISetOperand setOperand = getModelForSetOperand(thirdCat(), thirdValue());

	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new SetTestModel(operand, (Boolean) secondValue(), setOperand));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private Pair<TokenCategory, Object> getResultForMultipleExistenceTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(secondCat(), secondValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new ExistenceTestModel((Boolean) firstValue(), (EntQuery) operand));
	}
	final LogicalOperator logicalOperator = TokenCategory.ANY_OF_EQUERY_TOKENS.equals(secondCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }
}