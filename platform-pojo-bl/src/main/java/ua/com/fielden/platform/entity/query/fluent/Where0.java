package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;

final class Where0<ET extends AbstractEntity<?>> //
		extends Where<IComparisonOperator0<ET>, ICompoundCondition0<ET>, IWhere1<ET>, ET> //
		implements IWhere0<ET> {

    protected Where0(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IWhere1<ET> nextForWhere(final Tokens tokens) {
		return new Where1<ET>(tokens);
	}

	@Override
	protected ICompoundCondition0<ET> nextForConditionalOperand(final Tokens tokens) {
		return new CompoundCondition0<ET>(tokens);
	}

	@Override
	protected IComparisonOperator0<ET> nextForSingleOperand(final Tokens tokens) {
		return new ComparisonOperator0<ET>(tokens);
	}
}