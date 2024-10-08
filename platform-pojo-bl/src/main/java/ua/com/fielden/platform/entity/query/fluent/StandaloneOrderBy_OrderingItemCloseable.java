package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderByEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderByOffset;
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

        @Override
    public IOrderByOffset limit(final long n) {
        return new StandaloneOrderBy_OrderingItemCloseable(builder.limit(n));
    }

    @Override
    public IOrderByOffset limit(final Limit limit) {
        return new StandaloneOrderBy_OrderingItemCloseable(builder.limit(limit));
    }

    @Override
    public IOrderByEnd offset(final long n) {
        return new StandaloneOrderBy_OrderingItemCloseable(builder.offset(n));
    }

}
