package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;

final class CompoundCondition2<ET extends AbstractEntity<?>> //
		extends AbstractCompoundCondition<IWhere2<ET>, ICompoundCondition1<ET>> //
		implements ICompoundCondition2<ET> {

	@Override
	protected IWhere2<ET> nextForAbstractLogicalCondition() {
		return new Where2<ET>();
	}

	@Override
	protected ICompoundCondition1<ET> nextForAbstractCompoundCondition() {
		return new CompoundCondition1<ET>();
	}
}