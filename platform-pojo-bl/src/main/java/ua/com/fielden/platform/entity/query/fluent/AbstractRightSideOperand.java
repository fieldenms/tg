package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IQuantifiedOperand;
import ua.com.fielden.platform.entity.query.model.SingleResultQueryModel;

abstract class AbstractRightSideOperand<T, ET extends AbstractEntity<?>> extends AbstractMultipleOperand<T, ET> implements IQuantifiedOperand<T, ET> {

    @Override
    public T any(final SingleResultQueryModel subQuery) {
        return copy(nextForAbstractSingleOperand(), getTokens().any(subQuery));
    }

    @Override
    public T all(final SingleResultQueryModel subQuery) {
        return copy(nextForAbstractSingleOperand(), getTokens().all(subQuery));
    }
}