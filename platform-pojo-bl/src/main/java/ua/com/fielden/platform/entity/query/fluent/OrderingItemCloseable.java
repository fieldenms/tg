package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

class OrderingItemCloseable extends OrderingItem implements IOrderingItemCloseable {

    OrderingItemCloseable(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public OrderingModel model() {
        return new OrderingModel(getTokens().getValues());
    }
}