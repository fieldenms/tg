package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;

public final class StandAloneConditionOperand<ET extends AbstractEntity<?>> //
        extends WhereWithoutNesting<IStandAloneConditionComparisonOperator<ET>, IStandAloneConditionCompoundCondition<ET>, ET> //
        implements IStandAloneConditionOperand<ET> {

    public StandAloneConditionOperand(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    protected IStandAloneConditionCompoundCondition<ET> nextForConditionalOperand(final EqlSentenceBuilder builder) {
        return new StandAloneConditionCompoundCondition<ET>(builder);
    }

    @Override
    protected IStandAloneConditionComparisonOperator<ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
        return new StandAloneConditionComparisonOperator<ET>(builder);
    }

}
