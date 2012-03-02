package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

final class SingleOperandOrderable extends AbstractQueryLink /*OrderingItem*/ implements ISingleOperandOrderable {
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
}
