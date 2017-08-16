package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;

final class ComparisonOperator2<ET extends AbstractEntity<?>> //
		extends AbstractComparisonOperator<ICompoundCondition2<ET>, ET> //
		implements IComparisonOperator2<ET> {

	@Override
	protected ICompoundCondition2<ET> nextForAbstractComparisonOperator() {
		return new CompoundCondition2<ET>();
	}
}