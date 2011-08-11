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
import ua.com.fielden.platform.entity.query.model.elements.LogicalOperator;
import ua.com.fielden.platform.entity.query.model.elements.NullTestModel;
import ua.com.fielden.platform.entity.query.model.structure.ICondition;
import ua.com.fielden.platform.entity.query.model.structure.ISingleOperand;
import ua.com.fielden.platform.entity.query.tokens.TokenCategory;
import ua.com.fielden.platform.utils.Pair;

/**
* existence test, grouped conditions, null test, like test, comparison test, set test, quantified test:
*
* COMPARISON_TEST: ANY vs 1, 1 vs ANY, ANY vs ANY
* LIKE_TEST: ANY vs 1, 1 vs ANY, ANY vs ANY
* IN_TEST: ANY vs 1
* NULL_TEST: ANY vs 1
* QUANTIFIED_TEST: ANY vs 1
*/
public class ConditionBuilder extends AbstractTokensBuilder {
    private final static List<TokenCategory> singleOperands = Arrays.asList(new TokenCategory[] {TokenCategory.PROP, TokenCategory.PARAM, TokenCategory.VAL, TokenCategory.EXPR, TokenCategory.FUNCTION_MODEL, TokenCategory.EQUERY_TOKENS, TokenCategory.EXPR_TOKENS});
    private final static List<TokenCategory> mutlipleAnyOperands = Arrays.asList(new TokenCategory[] {TokenCategory.ANY_OF_PROPS, TokenCategory.ANY_OF_PARAMS, TokenCategory.ANY_OF_VALUES, TokenCategory.ANY_OF_EQUERY_TOKENS, TokenCategory.ANY_OF_EXPR_TOKENS});
    private final static List<TokenCategory> mutlipleAllOperands = Arrays.asList(new TokenCategory[] {TokenCategory.ALL_OF_PROPS, TokenCategory.ALL_OF_PARAMS, TokenCategory.ALL_OF_VALUES, TokenCategory.ALL_OF_EQUERY_TOKENS, TokenCategory.ALL_OF_EXPR_TOKENS});
    private final static List<TokenCategory> mutlipleOperands = new ArrayList<TokenCategory>();
    private final static List<TokenCategory> setOperands = Arrays.asList(new TokenCategory[] {TokenCategory.SET_OF_PROPS, TokenCategory.SET_OF_PARAMS, TokenCategory.SET_OF_VALUES, TokenCategory.EQUERY_TOKENS, TokenCategory.SET_OF_EXPR_TOKENS});
    private final static List<TokenCategory> quantifiers = Arrays.asList(new TokenCategory[] {TokenCategory.ANY_OPERATOR, TokenCategory.ALL_OPERATOR});
    {
	mutlipleOperands.addAll(mutlipleAllOperands);
	mutlipleOperands.addAll(mutlipleAnyOperands);
    }

    protected ConditionBuilder(final AbstractTokensBuilder parent) {
	super(parent);
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

    private boolean isPlainComparisonTest() {
	return getSize() == 3 && singleOperands.contains(firstCat()) && TokenCategory.COMPARISON_OPERATOR.equals(secondCat()) && singleOperands.contains(thirdCat());
    }

    private boolean isSingleVsMultiplePlainComparisonTest() {
	return getSize() == 3 && singleOperands.contains(firstCat()) && TokenCategory.COMPARISON_OPERATOR.equals(secondCat()) && mutlipleOperands.contains(thirdCat());
    }

    private boolean isMultipleVsSingleComparisonTest() {
	return getSize() == 3 && mutlipleOperands.contains(firstCat()) && TokenCategory.COMPARISON_OPERATOR.equals(secondCat()) && singleOperands.contains(thirdCat());
    }

    private boolean isMultipleVsMultipleComparisonTest() {
	return getSize() == 3 && mutlipleOperands.contains(firstCat()) && TokenCategory.COMPARISON_OPERATOR.equals(secondCat()) && mutlipleOperands.contains(thirdCat());
    }

    private boolean isPlainLikeTest() {
	return getSize() == 3 && singleOperands.contains(firstCat()) && TokenCategory.LIKE_OPERATOR.equals(secondCat()) && singleOperands.contains(thirdCat());
    }

    private boolean isPlainILikeTest() {
	return getSize() == 3 && singleOperands.contains(firstCat()) && TokenCategory.ILIKE_OPERATOR.equals(secondCat()) && singleOperands.contains(thirdCat());
    }

    private boolean isSingleVsMultiplePlainLikeTest() {
	return getSize() == 3 && singleOperands.contains(firstCat()) && TokenCategory.LIKE_OPERATOR.equals(secondCat()) && mutlipleOperands.contains(thirdCat());
    }

    private boolean isMultipleVsSingleLikeTest() {
	return getSize() == 3 && mutlipleOperands.contains(firstCat()) && TokenCategory.LIKE_OPERATOR.equals(secondCat()) && singleOperands.contains(thirdCat());
    }

    private boolean isMultipleVsMultipleLikeTest() {
	return getSize() == 3 && mutlipleOperands.contains(firstCat()) && TokenCategory.LIKE_OPERATOR.equals(secondCat()) && mutlipleOperands.contains(thirdCat());
    }

    private boolean isPlainSetTest() {
	return getSize() == 3 && singleOperands.contains(firstCat()) && TokenCategory.IN_OPERATOR.equals(secondCat()) && setOperands.contains(thirdCat());
    }

    private boolean isMultipleSetTest() {
	return getSize() == 3 && mutlipleOperands.contains(firstCat()) && TokenCategory.IN_OPERATOR.equals(secondCat()) && setOperands.contains(thirdCat());
    }

    private boolean isPlainQuantifiedTest() {
	return getSize() == 3 && singleOperands.contains(firstCat()) && TokenCategory.COMPARISON_OPERATOR.equals(secondCat()) && quantifiers.contains(thirdCat());
    }

    private boolean isMultipleQuantifiedTest() {
	return getSize() == 3 && mutlipleOperands.contains(firstCat()) && TokenCategory.COMPARISON_OPERATOR.equals(secondCat()) && quantifiers.contains(thirdCat());
    }

    @Override
    public boolean isClosing() {
	return isPlainExistenceTest() || isMultipleExistenceTest() || isGroupOfConditions() || //
	isPlainNullTest() || isMultipleNullTest() || //
	isPlainComparisonTest() || isMultipleVsSingleComparisonTest() || isMultipleVsMultipleComparisonTest() || isSingleVsMultiplePlainComparisonTest() || //
	isPlainLikeTest() || isMultipleVsSingleLikeTest() || isMultipleVsMultipleLikeTest() || isSingleVsMultiplePlainLikeTest() || //
	isPlainILikeTest() ||
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
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isMultipleVsMultipleComparisonTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isSingleVsMultiplePlainComparisonTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isPlainLikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isMultipleVsSingleLikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isMultipleVsMultipleLikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isSingleVsMultiplePlainLikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isPlainILikeTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isPlainSetTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isMultipleSetTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isPlainQuantifiedTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else if (isMultipleQuantifiedTest()) {
	    throw new RuntimeException("Unable to get result - not implemented yet");
	} else {
	    throw new RuntimeException("Unrecognised result");
	}
    }

    private Pair<TokenCategory, Object> getResultForGroupOfConditions() {
	return getTokens().get(0);
    }

    private Pair<TokenCategory, Object> getResultForPlainNullTest() {
	final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
	return new Pair<TokenCategory, Object>(TokenCategory.NULL_TEST, new NullTestModel(operand, (Boolean) secondValue()));
    }

    private Pair<TokenCategory, Object> getResultForPlainComparisonTest() {
	final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
	final ISingleOperand secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
	return new Pair<TokenCategory, Object>(TokenCategory.COMPARISON_TEST, new ComparisonTestModel(firstOperand, (ComparisonOperator) secondValue(), secondOperand));
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

    private Pair<TokenCategory, Object> getResultForMultipleNullTest() {
	final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
	final List<ICondition> conditions = new ArrayList<ICondition>();
	for (final ISingleOperand operand : operands) {
	    conditions.add(new NullTestModel(operand, (Boolean) secondValue()));
	}
	final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? LogicalOperator.OR : LogicalOperator.AND;
	return new Pair<TokenCategory, Object>(TokenCategory.GROUPED_CONDITIONS, getGroup(conditions, logicalOperator));
    }

    private Pair<TokenCategory, Object> getResultForPlainExistenceTest() {
	return new Pair<TokenCategory, Object>(TokenCategory.EXISTENCE_TEST, new ExistenceTestModel((Boolean) firstValue(), new QueryBuilder((QueryModel) secondValue()).getQry()));
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