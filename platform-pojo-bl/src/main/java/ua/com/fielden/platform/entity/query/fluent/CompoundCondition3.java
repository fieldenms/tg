package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class CompoundCondition3<ET extends AbstractEntity<?>> //
		extends CompoundCondition<IWhere3<ET>, ICompoundCondition2<ET>> //
		implements ICompoundCondition3<ET> {

	public CompoundCondition3(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	protected IWhere3<ET> nextForLogicalCondition(final EqlSentenceBuilder builder) {
		return new Where3<ET>(builder);
	}

	@Override
	protected ICompoundCondition2<ET> nextForCompoundCondition(final EqlSentenceBuilder builder) {
		return new CompoundCondition2<ET>(builder);
	}

}
