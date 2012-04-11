package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IQuantifiedOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class AbstractRightSideOperand<T> extends AbstractMultipleOperand<T> implements IQuantifiedOperand<T> {

    protected AbstractRightSideOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T any(final SingleResultQueryModel subQuery) {
	return copy(getParent(), getTokens().any(subQuery));
    }

    @Override
    public T all(final SingleResultQueryModel subQuery) {
	return copy(getParent(), getTokens().all(subQuery));
    }
}
