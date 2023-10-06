package ua.com.fielden.platform.eql.stage0;

import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.AND;
import static ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator.OR;
import static ua.com.fielden.platform.entity.query.fluent.enums.Quantifier.ALL;
import static ua.com.fielden.platform.entity.query.fluent.enums.Quantifier.ANY;
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
import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ua.com.fielden.platform.entity.query.fluent.LikeOptions;
import ua.com.fielden.platform.entity.query.fluent.enums.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.enums.Quantifier;
import ua.com.fielden.platform.entity.query.fluent.enums.TokenCategory;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.stage1.conditions.ComparisonPredicate1;
import ua.com.fielden.platform.eql.stage1.conditions.CompoundCondition1;
import ua.com.fielden.platform.eql.stage1.conditions.Conditions1;
import ua.com.fielden.platform.eql.stage1.conditions.ExistencePredicate1;
import ua.com.fielden.platform.eql.stage1.conditions.ICondition1;
import ua.com.fielden.platform.eql.stage1.conditions.LikePredicate1;
import ua.com.fielden.platform.eql.stage1.conditions.NullPredicate1;
import ua.com.fielden.platform.eql.stage1.conditions.QuantifiedPredicate1;
import ua.com.fielden.platform.eql.stage1.conditions.SetPredicate1;
import ua.com.fielden.platform.eql.stage1.operands.ISetOperand1;
import ua.com.fielden.platform.eql.stage1.operands.ISingleOperand1;
import ua.com.fielden.platform.eql.stage1.operands.queries.SubQuery1;
import ua.com.fielden.platform.eql.stage2.conditions.ICondition2;
import ua.com.fielden.platform.eql.stage2.operands.ISetOperand2;
import ua.com.fielden.platform.eql.stage2.operands.ISingleOperand2;
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

    public ConditionBuilder(final AbstractTokensBuilder parent, final QueryModelToStage1Transformer queryBuilder) {
        super(parent, queryBuilder);
    }

    private boolean isPlainExistencePredicate() {
        return getSize() == 2 && EXISTS_OPERATOR == firstCat() && EQUERY_TOKENS == secondCat();
    }

    private boolean isMultipleExistencePredicate() {
        return getSize() == 2 && EXISTS_OPERATOR == firstCat() && (ANY_OF_EQUERY_TOKENS == secondCat() || ALL_OF_EQUERY_TOKENS == secondCat());
    }

    private boolean isGroupOfConditions() {
        return getSize() == 1 && GROUPED_CONDITIONS == firstCat();
    }

    private boolean isPlainNullPredicate() {
        return getSize() == 2 && singleOperands.contains(firstCat()) && NULL_OPERATOR == secondCat();
    }

    private boolean isMultipleNullPredicate() {
        return getSize() == 2 && mutlipleOperands.contains(firstCat()) && NULL_OPERATOR == secondCat();
    }

    private boolean testThreeSome(final List<TokenCategory> leftRange, final TokenCategory operator, final List<TokenCategory> rightRange) {
        return getSize() == 3 && leftRange.contains(firstCat()) && operator == secondCat() && rightRange.contains(thirdCat());
    }

    private boolean isPlainComparisonPredicate() {
        return testThreeSome(singleOperands, COMPARISON_OPERATOR, singleOperands);
    }

    private boolean isSingleVsMultiplePlainComparisonPredicate() {
        return testThreeSome(singleOperands, COMPARISON_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsSingleComparisonPredicate() {
        return testThreeSome(mutlipleOperands, COMPARISON_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsMultipleComparisonPredicate() {
        return testThreeSome(mutlipleOperands, COMPARISON_OPERATOR, mutlipleOperands);
    }

    private boolean isPlainLikePredicate() {
        return testThreeSome(singleOperands, LIKE_OPERATOR, singleOperands);
    }

    private boolean isSingleVsMultipleLikePredicate() {
        return testThreeSome(singleOperands, LIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isMultipleVsSingleLikePredicate() {
        return testThreeSome(mutlipleOperands, LIKE_OPERATOR, singleOperands);
    }

    private boolean isMultipleVsMultipleLikePredicate() {
        return testThreeSome(mutlipleOperands, LIKE_OPERATOR, mutlipleOperands);
    }

    private boolean isPlainSetPredicate() {
        return testThreeSome(singleOperands, IN_OPERATOR, setOperands);
    }

    private boolean isMultipleSetPredicate() {
        return testThreeSome(mutlipleOperands, IN_OPERATOR, setOperands);
    }

    private boolean isPlainQuantifiedPredicate() {
        return testThreeSome(singleOperands, COMPARISON_OPERATOR, quantifiers);
    }

    private boolean isMultipleQuantifiedPredicate() {
        return testThreeSome(mutlipleOperands, COMPARISON_OPERATOR, quantifiers);
    }

    @Override
    public boolean isClosing() {
        return isPlainExistencePredicate() || isMultipleExistencePredicate() || isGroupOfConditions() || //
                isPlainNullPredicate() || isMultipleNullPredicate() || //
                isPlainComparisonPredicate() || isMultipleVsSingleComparisonPredicate() || isMultipleVsMultipleComparisonPredicate() || isSingleVsMultiplePlainComparisonPredicate() || //
                isPlainLikePredicate() || isMultipleVsSingleLikePredicate() || isMultipleVsMultipleLikePredicate() || isSingleVsMultipleLikePredicate() || //
                isPlainSetPredicate() || isMultipleSetPredicate() || isPlainQuantifiedPredicate() || isMultipleQuantifiedPredicate() //
        ;
    }

    private Object getResultantCondition() {
        if (isPlainNullPredicate()) {
            return getPlainNullPredicate();
        } else if (isMultipleNullPredicate()) {
            return getMultipleNullPredicate();
        } else if (isGroupOfConditions()) {
            return getTokens().get(0).getValue();
        } else if (isPlainExistencePredicate()) {
            return getPlainExistencePredicate();
        } else if (isMultipleExistencePredicate()) {
            return getMultipleExistencePredicate();
        } else if (isPlainComparisonPredicate()) {
            return getPlainComparisonPredicate();
        } else if (isMultipleVsSingleComparisonPredicate()) {
            return getMultipleVsSingleComparisonPredicate();
        } else if (isMultipleVsMultipleComparisonPredicate()) {
            return getMultipleVsMultipleComparisonPredicate();
        } else if (isSingleVsMultiplePlainComparisonPredicate()) {
            return getSingleVsMultipleComparisonPredicate();
        } else if (isPlainLikePredicate()) {
            return getPlainLikePredicate();
        } else if (isMultipleVsSingleLikePredicate()) {
            return getMultipleVsSingleLikePredicate();
        } else if (isMultipleVsMultipleLikePredicate()) {
            return getMultipleVsMultipleLikePredicate();
        } else if (isSingleVsMultipleLikePredicate()) {
            return getSingleVsMultipleLikePredicate();
        } else if (isPlainSetPredicate()) {
            return getPlainSetPredicate();
        } else if (isMultipleSetPredicate()) {
            return getMultipleSetPredicate();
        } else if (isPlainQuantifiedPredicate()) {
            return getPlainQuantifiedPredicate();
        } else if (isMultipleQuantifiedPredicate()) {
            return getMultipleQuantifiedPredicate();
        } else {
            throw new RuntimeException("Unrecognised result");
        }
    }

    @Override
    public Pair<TokenCategory, Object> getResult() {
        return new Pair<TokenCategory, Object>(CONDITION, getResultantCondition());
    }

    private ICondition1<? extends ICondition2<?>> getPlainQuantifiedPredicate() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final SubQuery1 secondOperand = getQueryBuilder().generateAsSubquery((QueryModel<?>) thirdValue());
        final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
        return new QuantifiedPredicate1(firstOperand, (ComparisonOperator) secondValue(), quantifier, secondOperand);
    }

    private Conditions1 getMultipleQuantifiedPredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final SubQuery1 secondOperand = getQueryBuilder().generateAsSubquery((QueryModel<?>) thirdValue());
        final Quantifier quantifier = ANY_OPERATOR == thirdCat() ? ANY : ALL;
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new QuantifiedPredicate1(operand, (ComparisonOperator) secondValue(), quantifier, secondOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getGroup(final List<ICondition1<? extends ICondition2<?>>> conditions, final LogicalOperator logicalOperator) {
        final Iterator<ICondition1<? extends ICondition2<?>>> iterator = conditions.iterator();
        if (!iterator.hasNext()) {
            return Conditions1.emptyConditions;
        } else {
            final ICondition1<? extends ICondition2<?>> firstCondition = iterator.next();
            final List<CompoundCondition1> otherConditions = new ArrayList<>();
            for (; iterator.hasNext();) {
                final CompoundCondition1 subsequentCompoundCondition = new CompoundCondition1(logicalOperator, iterator.next());
                otherConditions.add(subsequentCompoundCondition);
            }
            return new Conditions1(false, firstCondition, otherConditions);
        }
    }

    private NullPredicate1 getPlainNullPredicate() {
        final ISingleOperand1<? extends ISingleOperand2<?>> operand = getModelForSingleOperand(firstCat(), firstValue());
        return new NullPredicate1(operand, (Boolean) secondValue());
    }

    private Conditions1 getMultipleNullPredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new NullPredicate1(operand, (Boolean) secondValue()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getMultipleVsMultipleComparisonPredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

        final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition1<? extends ICondition2<?>>> outerConditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand : leftOperands) {
            final List<ICondition1<? extends ICondition2<?>>> innerConditions = new ArrayList<>();
            for (final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand : rightOperands) {
                innerConditions.add(new ComparisonPredicate1(leftOperand, operator, rightOperand));
            }
            final Conditions1 group = getGroup(innerConditions, rightLogicalOperator);
            outerConditions.add(group);
        }
        return getGroup(outerConditions, leftLogicalOperator);
    }

    private Conditions1 getMultipleVsSingleComparisonPredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new ComparisonPredicate1(operand, operator, singleOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getSingleVsMultipleComparisonPredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ComparisonOperator operator = (ComparisonOperator) secondValue();

        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new ComparisonPredicate1(singleOperand, operator, operand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private ComparisonPredicate1 getPlainComparisonPredicate() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        return new ComparisonPredicate1(firstOperand, (ComparisonOperator) secondValue(), secondOperand);
    }

    private LikePredicate1 getPlainLikePredicate() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> secondOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        return new LikePredicate1(firstOperand, secondOperand, (LikeOptions) secondValue());
    }

    private Conditions1 getMultipleVsMultipleLikePredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> leftOperands = getModelForMultipleOperands(firstCat(), firstValue());
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> rightOperands = getModelForMultipleOperands(thirdCat(), thirdValue());

        final LogicalOperator leftLogicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        final LogicalOperator rightLogicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;

        final List<ICondition1<? extends ICondition2<?>>> outerConditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> leftOperand : leftOperands) {
            final List<ICondition1<? extends ICondition2<?>>> innerConditions = new ArrayList<>();
            for (final ISingleOperand1<? extends ISingleOperand2<?>> rightOperand : rightOperands) {
                innerConditions.add(new LikePredicate1(leftOperand, rightOperand, (LikeOptions) secondValue()));
            }
            final Conditions1 group = getGroup(innerConditions, rightLogicalOperator);
            outerConditions.add(group);
        }
        return getGroup(outerConditions, leftLogicalOperator);
    }

    private Conditions1 getMultipleVsSingleLikePredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(thirdCat(), thirdValue());
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new LikePredicate1(operand, singleOperand, (LikeOptions) secondValue()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getSingleVsMultipleLikePredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(thirdCat(), thirdValue());
        final ISingleOperand1<? extends ISingleOperand2<?>> singleOperand = getModelForSingleOperand(firstCat(), firstValue());
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new LikePredicate1(singleOperand, operand, (LikeOptions) secondValue()));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(thirdCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private ExistencePredicate1 getPlainExistencePredicate() {
        return new ExistencePredicate1((Boolean) firstValue(), getQueryBuilder().generateAsTypelessSubquery((QueryModel<?>) secondValue()));
    }

    private SetPredicate1 getPlainSetPredicate() {
        final ISingleOperand1<? extends ISingleOperand2<?>> firstOperand = getModelForSingleOperand(firstCat(), firstValue());
        final ISetOperand1<? extends ISetOperand2<?>> setOperand = getModelForSetOperand(thirdCat(), thirdValue());
        return new SetPredicate1(firstOperand, (Boolean) secondValue(), setOperand);
    }

    private Conditions1 getMultipleSetPredicate() {
        final List<ISingleOperand1<? extends ISingleOperand2<?>>> operands = getModelForMultipleOperands(firstCat(), firstValue());
        final ISetOperand1<? extends ISetOperand2<?>> setOperand = getModelForSetOperand(thirdCat(), thirdValue());

        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final ISingleOperand1<? extends ISingleOperand2<?>> operand : operands) {
            conditions.add(new SetPredicate1(operand, (Boolean) secondValue(), setOperand));
        }
        final LogicalOperator logicalOperator = mutlipleAnyOperands.contains(firstCat()) ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }

    private Conditions1 getMultipleExistencePredicate() {
        final List<ICondition1<? extends ICondition2<?>>> conditions = new ArrayList<>();
        for (final QueryModel<?> qm : (List<QueryModel<?>>) secondValue()) {
            conditions.add(new ExistencePredicate1((Boolean) firstValue(), getQueryBuilder().generateAsTypelessSubquery(qm)));
        }
        final LogicalOperator logicalOperator = ANY_OF_EQUERY_TOKENS == secondCat() ? OR : AND;
        return getGroup(conditions, logicalOperator);
    }
}