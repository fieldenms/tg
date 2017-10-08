package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

final class OrderingItemCloseable //
		extends OrderingItem //
		implements IOrderingItemCloseable {

    public OrderingItemCloseable(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public OrderingModel model() {
		return new OrderingModel(getTokens().getValues());
	}
}