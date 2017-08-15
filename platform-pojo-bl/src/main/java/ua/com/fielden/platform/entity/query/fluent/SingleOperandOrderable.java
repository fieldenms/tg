package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

final class SingleOperandOrderable extends AbstractQueryLink /*OrderingItem*/implements ISingleOperandOrderable {

    @Override
    public IOrderingItemCloseable asc() {
        return copy(new OrderingItemCloseable(), getTokens().asc());
    }

    @Override
    public IOrderingItemCloseable desc() {
        return copy(new OrderingItemCloseable(), getTokens().desc());
    }
}