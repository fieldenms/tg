package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;

final class CompoundCondition0<ET extends AbstractEntity<?>> //
		extends Completed<ET> //
		implements ICompoundCondition0<ET> {

	public CompoundCondition0(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public IWhere0<ET> and() {
		return new Where0<ET>(builder.and());
	}

	@Override
	public IWhere0<ET> or() {
		return new Where0<ET>(builder.or());
	}

}
