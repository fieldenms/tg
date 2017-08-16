package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;

final class ComparisonOperator0<ET extends AbstractEntity<?>> //
		extends AbstractComparisonOperator<ICompoundCondition0<ET>, ET> //
		implements IComparisonOperator0<ET> {

	@Override
	protected ICompoundCondition0<ET> nextForAbstractComparisonOperator() {
		return new CompoundCondition0<ET>();
	}
}