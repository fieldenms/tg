package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISubsequentCompletedAndYielded;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

/**
 * If Java had multiple inheritance, then would also extend {@link CompletedAndYielded}. We work around this by using
 * {@link CompletedAndYielded} as a delegate.
 */
final class OrderingItem<ET extends AbstractEntity<?>>
        extends OrderingItem1<ET>
        implements IOrderingItem<ET>
{

    public OrderingItem(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public IFunctionYieldedLastArgument<IFirstYieldedItemAlias<ISubsequentCompletedAndYielded<ET>>, ET> yield() {
        return new CompletedAndYielded<ET>(builder).yield();
    }

    @Override
    public ISubsequentCompletedAndYielded<ET> yieldAll() {
        return new CompletedAndYielded<ET>(builder).yieldAll();
    }

    @Override
    public EntityResultQueryModel<ET> model() {
        return new CompletedAndYielded<ET>(builder).model();
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
        return new CompletedAndYielded<ET>(builder).modelAsEntity(resultType);
    }

    @Override
    public AggregatedResultQueryModel modelAsAggregate() {
        return new CompletedAndYielded<ET>(builder).modelAsAggregate();
    }

}
