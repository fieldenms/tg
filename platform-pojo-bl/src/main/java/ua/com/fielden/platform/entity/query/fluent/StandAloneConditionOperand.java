package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionCompoundCondition;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IStandAloneConditionOperand;

public final class StandAloneConditionOperand<ET extends AbstractEntity<?>> //
		extends WhereWithoutNesting<IStandAloneConditionComparisonOperator<ET>, IStandAloneConditionCompoundCondition<ET>, ET> //
		implements IStandAloneConditionOperand<ET> {

	@Override
	protected IStandAloneConditionCompoundCondition<ET> nextForAbstractConditionalOperand() {
		return new StandAloneConditionCompoundCondition<ET>();
	}

	@Override
	protected IStandAloneConditionComparisonOperator<ET> nextForAbstractSingleOperand() {
		return new StandAloneConditionComparisonOperator<ET>();
	}
}