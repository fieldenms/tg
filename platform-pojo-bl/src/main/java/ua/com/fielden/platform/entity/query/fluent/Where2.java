package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class Where2<ET extends AbstractEntity<?>> //
		extends Where<IComparisonOperator2<ET>, ICompoundCondition2<ET>, IWhere3<ET>, ET> //
		implements IWhere2<ET> {

    public Where2(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IWhere3<ET> nextForWhere(final Tokens tokens) {
		return new Where3<ET>(tokens);
	}

	@Override
	protected ICompoundCondition2<ET> nextForConditionalOperand(final Tokens tokens) {
		return new CompoundCondition2<ET>(tokens);
	}

	@Override
	protected IComparisonOperator2<ET> nextForSingleOperand(final Tokens tokens) {
		return new ComparisonOperator2<ET>(tokens);
	}
}