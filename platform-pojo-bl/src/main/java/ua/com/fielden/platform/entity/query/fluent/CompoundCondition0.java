package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;

final class CompoundCondition0<ET extends AbstractEntity<?>> //
		extends Completed<ET> //
		implements ICompoundCondition0<ET> {

	@Override
	public IWhere0<ET> and() {
		return copy(new Where0<ET>(), getTokens().and());
	}

	@Override
	public IWhere0<ET> or() {
		return copy(new Where0<ET>(), getTokens().or());
	}
}