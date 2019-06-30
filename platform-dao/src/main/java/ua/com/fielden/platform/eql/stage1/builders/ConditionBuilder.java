package ua.com.fielden.platform.eql.stage1.builders;

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
import static ua.com.fielden.platform.eql.meta.Quantifier.ALL;
import static ua.com.fielden.platform.eql.meta.Quantifier.ANY;
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.meta.Quantifier;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ComparisonTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ExistenceTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.LikeTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.NullTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.QuantifiedTest1;
import ua.com.fielden.platform.eql.stage1.elements.conditions.SetTest1;
import ua.com.fielden.platform.eql.stage1.elements.operands.EntQuery1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.elements.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage2.elements.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.elements.operands.ISingleOperand2;
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

    protected ConditionBuilder(final AbstractTokensBuilder parent, final EntQueryGenerator queryBuilder) {
        super(parent, queryBuilder);
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
        return new Pair<TokenCategory, Object>(CONDITION, getResultantCondition());
    }

    private ICondition1<? extends ICondition2<?>> getPlainQuantifiedTest() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final EntQuery1 secondOperand = (EntQuery1) getModelForSingleOperand(thirdCat(), thirdValue());
        final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
        return new QuantifiedTest1(firstOperand, (ComparisonOperator) secondValue(), quantifier, secondOperand);
    }

    private Conditions1 getMultipleQuantifiedTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final EntQuery1 secondOperand = (EntQuery1) getModelForSingleOperand(thirdCat(), thirdValue());
        final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new QuantifiedTest1(operand, (ComparisonOperator) secondValue(), quantifier, secondOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getGroup(final List<ICondition1<? extends ICondition2<?>>> conditions, final LogicalOperator logicalOperator) {
        final Iterator<ICondition1<? extends ICondition2<?>>> iterator = conditions.iterator();
        if (!iterator.hasNext()) {
            return new Conditions1();
        } else {
            final ICondition1<? extends ICondition2<?>> firstCondition = iterator.next();
            final List<CompoundCondition1> otherConditions = new ArrayList<CompoundCondition1>();
            for (; iterator.hasNext();) {
                final CompoundCondition1 subsequentCompoundCondition = new CompoundCondition1(logicalOperator, iterator.next());
                otherConditions.add(subsequentCompoundCondition);
            }
            return new Conditions1(false, firstCondition, otherConditions);
        }
    }

    private NullTest1 getPlainNullTest() {
        final ISingleOperand1<? extends ISingleOperand2<?>> operand = getModelForSingleOperand(firstCat(), firstValue());
        return new NullTest1(operand, (Boolean) secondValue());
    }

    private Conditions1 getMultipleNullTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new NullTest1(operand, (Boolean) secondValue()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getMultipleVsMultipleComparisonTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

        final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition1<? extends ICondition2<?>>> outerConditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand : leftOperands) {
            final List<ICondition1<? extends ICondition2<?>>> innerConditions = new ArrayList<>();
            for (final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand : rightOperands) {
                innerConditions.add(new ComparisonTest1(leftOperand, operator, rightOperand));
            }
            final Conditions1 group = getGroup(innerConditions, rightLogicalOperator);
            outerConditions.add(group);
        }
        return getGroup(outerConditions, leftLogicalOperator);
    }

    private Conditions1 getMultipleVsSingleComparisonTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new ComparisonTest1(operand, operator, singleOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getSingleVsMultipleComparisonTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new ComparisonTest1(singleOperand, operator, operand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private ComparisonTest1 getPlainComparisonTest() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        return new ComparisonTest1(firstOperand, (ComparisonOperator) secondValue(), secondOperand);
    }

    private LikeTest1 getPlainLikeTest() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        return new LikeTest1(firstOperand, secondOperand, (LikeOptions) secondValue());
    }

    private Conditions1 getMultipleVsMultipleLikeTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

        final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

        final List<ICondition1<? extends ICondition2<?>>> outerConditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand : leftOperands) {
            final List<ICondition1<? extends ICondition2<?>>> innerConditions = new ArrayList<>();
            for (final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand : rightOperands) {
                innerConditions.add(new LikeTest1(leftOperand, rightOperand, (LikeOptions) secondValue()));
            }
            final Conditions1 group = getGroup(innerConditions, rightLogicalOperator);
            outerConditions.add(group);
        }
        return getGroup(outerConditions, leftLogicalOperator);
    }

    private Conditions1 getMultipleVsSingleLikeTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new LikeTest1(operand, singleOperand, (LikeOptions) secondValue()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getSingleVsMultipleLikeTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(firstCat(), firstValue());
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new LikeTest1(singleOperand, operand, (LikeOptions) secondValue()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private ExistenceTest1 getPlainExistenceTest() {
        return new ExistenceTest1((Boolean) firstValue(), getQueryBuilder().generateEntQueryAsSubquery((QueryModel<?>) secondValue()));
    }

    private SetTest1 getPlainSetTest() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ISetOperand1<? extends ISetOperand2<?>> setOperand = getModelForSetOperand(thirdCat(), thirdValue());
        return new SetTest1(firstOperand, (Boolean) secondValue(), setOperand);
    }

    private Conditions1 getMultipleSetTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final ISetOperand1<? extends ISetOperand2<?>> setOperand = getModelForSetOperand(thirdCat(), thirdValue());

        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new SetTest1(operand, (Boolean) secondValue(), setOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getMultipleExistenceTest() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(secondCat(), secondValue());
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new ExistenceTest1((Boolean) firstValue(), (EntQuery1) operand));
        }
        final LogicalOperator logicalOperator = ANY_OF_EQUERY_TOKENS == secondCat() ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }
}