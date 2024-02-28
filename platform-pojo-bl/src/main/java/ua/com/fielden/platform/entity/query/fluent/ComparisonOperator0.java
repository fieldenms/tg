package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;

final class ComparisonOperator0<ET extends AbstractEntity<?>> //
		extends ComparisonOperator<ICompoundCondition0<ET>, ET> //
		implements IComparisonOperator0<ET> {

	public ComparisonOperator0(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	protected ICompoundCondition0<ET> nextForComparisonOperator(final EqlSentenceBuilder builder) {
		return new CompoundCondition0<ET>(builder);
	}

}
