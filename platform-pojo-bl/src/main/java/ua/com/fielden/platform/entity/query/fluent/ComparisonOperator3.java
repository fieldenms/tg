package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;

final class ComparisonOperator3<ET extends AbstractEntity<?>> //
		extends ComparisonOperator<ICompoundCondition3<ET>, ET> //
		implements IComparisonOperator3<ET> {

	@Override
	protected ICompoundCondition3<ET> nextForAbstractComparisonOperator() {
		return new CompoundCondition3<ET>();
	}
}