package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IQuantifiedOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class RightSideOperand<T, ET extends AbstractEntity<?>> //
		extends MultipleOperand<T, ET> //
		implements IQuantifiedOperand<T, ET> {

    protected RightSideOperand(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public T any(final SingleResultQueryModel subQuery) {
		return nextForSingleOperand(getTokens().any(subQuery));
	}

	@Override
	public T all(final SingleResultQueryModel subQuery) {
		return nextForSingleOperand(getTokens().all(subQuery));
	}
}