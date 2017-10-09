package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;

final class Where1<ET extends AbstractEntity<?>> //
		extends Where<IComparisonOperator1<ET>, ICompoundCondition1<ET>, IWhere2<ET>, ET> //
		implements IWhere1<ET> {

    public Where1(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IWhere2<ET> nextForWhere(final Tokens tokens) {
		return new Where2<ET>(tokens);
	}

	@Override
	protected ICompoundCondition1<ET> nextForConditionalOperand(final Tokens tokens) {
		return new CompoundCondition1<ET>(tokens);
	}

	@Override
	protected IComparisonOperator1<ET> nextForSingleOperand(final Tokens tokens) {
		return new ComparisonOperator1<ET>(tokens);
	}
}