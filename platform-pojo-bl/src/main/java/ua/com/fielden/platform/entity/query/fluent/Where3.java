package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class Where3<ET extends AbstractEntity<?>> //
		extends ConditionalOperand<IComparisonOperator3<ET>, ICompoundCondition3<ET>, ET> //
		implements IWhere3<ET> {

	@Override
	protected ICompoundCondition3<ET> nextForAbstractConditionalOperand() {
		return new CompoundCondition3<ET>();
	}

	@Override
	protected IComparisonOperator3<ET> nextForAbstractSingleOperand() {
		return new ComparisonOperator3<ET>();
	}
}