package ua.com.fielden.platform.entity.query.generation;

import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.OR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ALL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ANY_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.COMPARISON_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.CONDITION;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EQUERY_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXISTS_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.EXT_PROP;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.FUNCTION_MODEL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.GROUPED_CONDITIONS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IN_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IPARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.IVAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.LIKE_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.NULL_OPERATOR;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PARAM;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.PROP;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_EXPR_TOKENS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_IPARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_PARAMS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_PROPS;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.SET_OF_VALUES;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.VAL;
import static ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory.ZERO_ARG_FUNCTION;
import static ua.com.fielden.platform.entity.query.generation.elements.Quantifier.ALL;
import static ua.com.fielden.platform.entity.query.generation.elements.Quantifier.ANY;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.generation.elements.ComparisonTest;
import ua.com.fielden.platform.entity.query.generation.elements.CompoundCondition;
import ua.com.fielden.platform.entity.query.generation.elements.EntQuery;
import ua.com.fielden.platform.entity.query.generation.elements.ExistenceTest;
import ua.com.fielden.platform.entity.query.generation.elements.GroupedConditions;
import ua.com.fielden.platform.entity.query.generation.elements.ICondition;
import ua.com.fielden.platform.entity.query.generation.elements.ISetOperand;
import ua.com.fielden.platform.entity.query.generation.elements.ISingleOperand;
import ua.com.fielden.platform.entity.query.generation.elements.LikeTest;
import ua.com.fielden.platform.entity.query.generation.elements.NullTest;
import ua.com.fielden.platform.entity.query.generation.elements.QuantifiedTest;
import ua.com.fielden.platform.entity.query.generation.elements.Quantifier;
import ua.com.fielden.platform.entity.query.generation.elements.SetTest;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.utils.Pair;

public class ConditionBuilder extends AbstractTokensBuilder {

    private final static List<TokenCategory> singleOperands = listOf(PROP, EXT_PROP, PARAM, IPARAM, VAL, IVAL, EXPR, FUNCTION_MODEL, ZERO_ARG_FUNCTION, EQUERY_TOKENS, EXPR_TOKENS);
    private final static List<TokenCategory> mutlipleAnyOperands = listOf(ANY_OF_PROPS, ANY_OF_PARAMS, ANY_OF_IPARAMS, ANY_OF_VALUES, ANY_OF_EQUERY_TOKENS, ANY_OF_EXPR_TOKENS);
    private final static List<TokenCategory> mutlipleAllOperands = listOf(ALL_OF_PROPS, ALL_OF_PARAMS, ALL_OF_IPARAMS, ALL_OF_VALUES, ALL_OF_EQUERY_TOKENS, ALL_OF_EXPR_TOKENS);
    private final static List<TokenCategory> setOperands = listOf(SET_OF_PROPS, SET_OF_PARAMS, SET_OF_IPARAMS, SET_OF_VALUES, EQUERY_TOKENS, SET_OF_EXPR_TOKENS);
    private final static List<TokenCategory> quantifiers = listOf(ANY_OPERATOR, ALL_OPERATOR);
    private final static List<TokenCategory> mutlipleOperands = new ArrayList<>();
    
    static {
        mutlipleOperands.addAll(mutlipleAllOperands);
        mutlipleOperands.addAll(mutlipleAnyOperands);
    }

    protected ConditionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder, final Map<String, Object> paramValues) {
        super(parent, queryBuilder, paramValues);
    }

    private boolean isPlainExistenceTest() {
        return getSize() == 2 && EXISTS_OPERATOR == firstCat() && EQUERY_TOKENS == secondCat();
    }

    private boolean isMultipleExistenceTest() {
        return getSize() == 2 && EXISTS_OPERATOR == firstCat() && (ANY_OF_EQUERY_TOKENS == secondCat() || ALL_OF_EQUERY_TOKENS == secondCat());
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

    private boolean isSingleVsMultipleLikeTest() {
        return testThreeSome(singleOperands, LIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsSingleLikeTest() {
        return testThreeSome(mutlipleOperands, LIKE_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsMultipleLikeTest() {
        return testThreeSome(mutlipleOperands, LIKE_OPERATOR, mutlipleOperands);
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
                isPlainSetTest() || isMultipleSetTest() || isPlainQuantifiedTest() || isMultipleQuantifiedTest() //
        ;
    }

    private Object getResultantCondition() {
        if (isPlainNullTest()) {
            return getPlainNullTest();
        } else if (isMultipleNullTest()) {
            return getMultipleNullTest();
        } else if (isGroupOfConditions()) {
            return getTokens().get(0).getValue();
        } else if (isPlainExistenceTest()) {
            return getPlainExistenceTest();
        } else if (isMultipleExistenceTest()) {
            return getMultipleExistenceTest();
        } else if (isPlainComparisonTest()) {
            return getPlainComparisonTest();
        } else if (isMultipleVsSingleComparisonTest()) {
            return getMultipleVsSingleComparisonTest();
        } else if (isMultipleVsMultipleComparisonTest()) {
            return getMultipleVsMultipleComparisonTest();
        } else if (isSingleVsMultiplePlainComparisonTest()) {
            return getSingleVsMultipleComparisonTest();
        } else if (isPlainLikeTest()) {
            return getPlainLikeTest();
        } else if (isMultipleVsSingleLikeTest()) {
            return getMultipleVsSingleLikeTest();
        } else if (isMultipleVsMultipleLikeTest()) {
            return getMultipleVsMultipleLikeTest();
        } else if (isSingleVsMultipleLikeTest()) {
            return getSingleVsMultipleLikeTest();
        } else if (isPlainSetTest()) {
            return getPlainSetTest();
        } else if (isMultipleSetTest()) {
            return getMultipleSetTest();
        } else if (isPlainQuantifiedTest()) {
            return getPlainQuantifiedTest();
        } else if (isMultipleQuantifiedTest()) {
            return getMultipleQuantifiedTest();
        } else {
            throw new RuntimeException("Unrecognised result");
        }
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        return new Pair<>(CONDITION, getResultantCondition());
    }

    private ICondition getPlainQuantifiedTest() {
        final ISingleOperand firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final EntQuery secondOperand = (EntQuery) getModelForSingleOperand(thirdCat(), thirdValue());
        final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
        return new QuantifiedTest(firstOperand, (ComparisonOperator) secondValue(), quantifier, secondOperand);
    }

    private GroupedConditions getMultipleQuantifiedTest() {
        final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final EntQuery secondOperand = (EntQuery) getModelForSingleOperand(thirdCat(), thirdValue());
        final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new QuantifiedTest(operand, (ComparisonOperator) secondValue(), quantifier, secondOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getGroup(final List<ICondition> conditions, final LogicalOperator logicalOperator) {
        final Iterator<ICondition> iterator = conditions.iterator();
        if (!iterator.hasNext()) {
            return new GroupedConditions(false, null);
        } else {
            final ICondition firstCondition = iterator.next();
            final List<CompoundCondition> otherConditions = new ArrayList<>();
            for (; iterator.hasNext();) {
                final CompoundCondition subsequentCompoundCondition = new CompoundCondition(logicalOperator, iterator.next());
                otherConditions.add(subsequentCompoundCondition);
            }
            return new GroupedConditions(false, firstCondition, otherConditions);
        }
    }

    private NullTest getPlainNullTest() {
        final ISingleOperand operand = getModelForSingleOperand(firstCat(), firstValue());
        return new NullTest(operand, (Boolean) secondValue(), getQueryBuilder().getDomainMetadataAnalyser());
    }

    private GroupedConditions getMultipleNullTest() {
        final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new NullTest(operand, (Boolean) secondValue(), getQueryBuilder().getDomainMetadataAnalyser()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getMultipleVsMultipleComparisonTest() {
        final List<ISingleOperand> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ISingleOperand> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

        final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition> outerConditions = new ArrayList<>();
        for (final ISingleOperand leftOperand : leftOperands) {
            final List<ICondition> innerConditions = new ArrayList<>();
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

        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new ComparisonTest(operand, operator, singleOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getSingleVsMultipleComparisonTest() {
        final List<ISingleOperand> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
        final ISingleOperand singleOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new ComparisonTest(singleOperand, operator, operand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
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
        return new LikeTest(firstOperand, secondOperand, (LikeOptions) secondValue(), getDbVersion());
    }

    private GroupedConditions getMultipleVsMultipleLikeTest() {
        final List<ISingleOperand> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ISingleOperand> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

        final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

        final List<ICondition> outerConditions = new ArrayList<>();
        for (final ISingleOperand leftOperand : leftOperands) {
            final List<ICondition> innerConditions = new ArrayList<>();
            for (final ISingleOperand rightOperand : rightOperands) {
                innerConditions.add(new LikeTest(leftOperand, rightOperand, (LikeOptions) secondValue(), getDbVersion()));
            }
            final GroupedConditions group = getGroup(innerConditions, rightLogicalOperator);
            outerConditions.add(group);
        }
        return getGroup(outerConditions, leftLogicalOperator);
    }

    private GroupedConditions getMultipleVsSingleLikeTest() {
        final List<ISingleOperand> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final ISingleOperand singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new LikeTest(operand, singleOperand, (LikeOptions) secondValue(), getDbVersion()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getSingleVsMultipleLikeTest() {
        final List<ISingleOperand> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
        final ISingleOperand singleOperand = getModelForSingleOperand(firstCat(), firstValue());
        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new LikeTest(singleOperand, operand, (LikeOptions) secondValue(), getDbVersion()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
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

        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new SetTest(operand, (Boolean) secondValue(), setOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private GroupedConditions getMultipleExistenceTest() {
        final List<ISingleOperand> operands = getModelForMultipleOperands(secondCat(), secondValue());
        final List<ICondition> conditions = new ArrayList<>();
        for (final ISingleOperand operand : operands) {
            conditions.add(new ExistenceTest((Boolean) firstValue(), (EntQuery) operand));
        }
        final LogicalOperator logicalOperator = ANY_OF_EQUERY_TOKENS == secondCat() ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }
}