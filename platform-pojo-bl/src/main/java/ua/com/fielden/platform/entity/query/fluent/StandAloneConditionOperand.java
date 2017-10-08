package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;

public final class StandAloneConditionOperand<ET extends AbstractEntity<?>> //
		extends WhereWithoutNesting<IStandAloneConditionComparisonOperator<ET>, IStandAloneConditionCompoundCondition<ET>, ET> //
		implements IStandAloneConditionOperand<ET> {

    public StandAloneConditionOperand(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IStandAloneConditionCompoundCondition<ET> nextForConditionalOperand(final Tokens tokens) {
		return new StandAloneConditionCompoundCondition<ET>(tokens);
	}

	@Override
	protected IStandAloneConditionComparisonOperator<ET> nextForSingleOperand(final Tokens tokens) {
		return new StandAloneConditionComparisonOperator<ET>(tokens);
	}
}