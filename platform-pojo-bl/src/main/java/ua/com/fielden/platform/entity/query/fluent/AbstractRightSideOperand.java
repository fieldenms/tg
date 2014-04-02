package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IQuantifiedOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class AbstractRightSideOperand<T, ET extends AbstractEntity<?>> extends AbstractMultipleOperand<T, ET> implements IQuantifiedOperand<T, ET> {

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