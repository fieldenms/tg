package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.model.OrderingModel;

final class StandaloneOrderBy_OrderingItemCloseable //
        extends StandaloneOrderBy_OrderingItem //
        implements IOrderingItemCloseable {

    public StandaloneOrderBy_OrderingItemCloseable(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public OrderingModel model() {
        return new OrderingModel(builder.model().getTokenSource());
    }

}
