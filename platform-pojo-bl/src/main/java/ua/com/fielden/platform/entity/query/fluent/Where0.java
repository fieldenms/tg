package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

final class Where0<ET extends AbstractEntity<?>> //
		extends Where<IComparisonOperator0<ET>, ICompoundCondition0<ET>, IWhere1<ET>, ET> //
		implements IWhere0<ET> {

	@Override
	protected IWhere1<ET> nextForWhere() {
		return new Where1<ET>();
	}

	@Override
	protected ICompoundCondition0<ET> nextForConditionalOperand() {
		return new CompoundCondition0<ET>();
	}

	@Override
	protected IComparisonOperator0<ET> nextForSingleOperand() {
		return new ComparisonOperator0<ET>();
	}
}