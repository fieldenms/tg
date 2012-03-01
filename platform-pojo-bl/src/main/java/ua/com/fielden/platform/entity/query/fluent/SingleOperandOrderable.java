package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

final class SingleOperandOrderable extends OrderingItem implements ISingleOperandOrderable {
    SingleOperandOrderable(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IOrderingItemCloseable asc() {
	return new OrderingItemCloseable(getTokens().asc());
    }

    @Override
    public IOrderingItemCloseable desc() {
	return new OrderingItemCloseable(getTokens().desc());
    }

    @Override
    public OrderingModel model() {
	return new OrderingModel(getTokens().getTokens());
    }
}
