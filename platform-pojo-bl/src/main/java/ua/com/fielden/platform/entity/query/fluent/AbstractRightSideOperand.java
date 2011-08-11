package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IQuantifiedOperand;
import ua.com.fielden.platform.entity.query.model.UnorderedQueryModel;

abstract class AbstractRightSideOperand<T> extends AbstractMultipleOperand<T> implements IQuantifiedOperand<T> {

    protected AbstractRightSideOperand(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T any(final UnorderedQueryModel subQuery) {
	getTokens().any(subQuery);
	return getParent();
    }

    @Override
    public T all(final UnorderedQueryModel subQuery) {
	getTokens().all(subQuery);
	return getParent();
    }
}
