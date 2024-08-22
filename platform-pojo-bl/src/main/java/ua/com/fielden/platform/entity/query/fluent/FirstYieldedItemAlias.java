package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFirstYieldedItemAlias;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;

abstract class FirstYieldedItemAlias<T> //
        extends AbstractQueryLink //
        implements IFirstYieldedItemAlias<T> {

    protected FirstYieldedItemAlias(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForFirstYieldedItemAlias(final EqlSentenceBuilder builder);

    @Override
    public T as(final CharSequence alias) {
        return nextForFirstYieldedItemAlias(builder.as(alias));
    }

    @Override
    public T asRequired(final CharSequence alias) {
        return nextForFirstYieldedItemAlias(builder.asRequired(alias));
    }

    @Override
    public <ET extends AbstractEntity<?>> EntityResultQueryModel<ET> modelAsEntity(final Class<ET> entityType) {
        return new EntityResultQueryModel<ET>(builder.modelAsEntity(entityType).getTokenSource(), entityType, builder.isYieldAll());
    }

    @Override
    public PrimitiveResultQueryModel modelAsPrimitive() {
        return new PrimitiveResultQueryModel(builder.modelAsPrimitive().getTokenSource());
    }

    @Override
    public T as(final Enum alias) {
        return as(alias.toString());
    }

    @Override
    public T asRequired(final Enum alias) {
        return asRequired(alias.toString());
    }

}
