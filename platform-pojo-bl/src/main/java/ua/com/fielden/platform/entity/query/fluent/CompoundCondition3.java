package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class CompoundCondition3<ET extends AbstractEntity<?>> //
		extends CompoundCondition<IWhere3<ET>, ICompoundCondition2<ET>> //
		implements ICompoundCondition3<ET> {

	@Override
	protected IWhere3<ET> nextForAbstractLogicalCondition() {
		return new Where3<ET>();
	}

	@Override
	protected ICompoundCondition2<ET> nextForAbstractCompoundCondition() {
		return new CompoundCondition2<ET>();
	}
}