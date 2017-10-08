package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere3;

final class CompoundCondition3<ET extends AbstractEntity<?>> //
		extends CompoundCondition<IWhere3<ET>, ICompoundCondition2<ET>> //
		implements ICompoundCondition3<ET> {

    public CompoundCondition3(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	protected IWhere3<ET> nextForLogicalCondition(final Tokens tokens) {
		return new Where3<ET>(tokens);
	}

	@Override
	protected ICompoundCondition2<ET> nextForCompoundCondition(final Tokens tokens) {
		return new CompoundCondition2<ET>(tokens);
	}
}