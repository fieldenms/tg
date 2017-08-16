package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

final class CompoundCondition1<ET extends AbstractEntity<?>> extends //
		CompoundCondition<IWhere1<ET>, ICompoundCondition0<ET>> //
		implements ICompoundCondition1<ET> {

	@Override
	protected IWhere1<ET> nextForLogicalCondition() {
		return new Where1<ET>();
	}

	@Override
	protected ICompoundCondition0<ET> nextForCompoundCondition() {
		return new CompoundCondition0<ET>();
	}
}