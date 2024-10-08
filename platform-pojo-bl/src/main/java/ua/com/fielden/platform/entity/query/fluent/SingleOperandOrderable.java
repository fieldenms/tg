package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItem;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

final class SingleOperandOrderable<ET extends AbstractEntity<?>>
        extends AbstractQueryLink
        implements ISingleOperandOrderable<ET> {

    public SingleOperandOrderable(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public IOrderingItem<ET> asc() {
        return new OrderingItem<>(builder.asc());
    }

    @Override
    public IOrderingItem<ET> desc() {
        return new OrderingItem<>(builder.desc());
    }

}
