package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable;

final class StandaloneOrderBy_SingleOperandOrderable //
        extends AbstractQueryLink //
        implements EntityQueryProgressiveInterfaces.StandaloneOrderBy.ISingleOperandOrderable {

    public StandaloneOrderBy_SingleOperandOrderable(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public EntityQueryProgressiveInterfaces.StandaloneOrderBy.IOrderingItemCloseable asc() {
        return new StandaloneOrderBy_OrderingItemCloseable(builder.asc());
    }

    @Override
    public IOrderingItemCloseable desc() {
        return new StandaloneOrderBy_OrderingItemCloseable(builder.desc());
    }

}
