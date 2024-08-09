package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IOrderingItemCloseable;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ISingleOperandOrderable;

final class SingleOperandOrderable //
		extends AbstractQueryLink //
		implements ISingleOperandOrderable {

	public SingleOperandOrderable(final EqlSentenceBuilder builder) {
		super(builder);
	}

	@Override
	public IOrderingItemCloseable asc() {
		return new OrderingItemCloseable(builder.asc());
	}

	@Override
	public IOrderingItemCloseable desc() {
		return new OrderingItemCloseable(builder.desc());
	}

}
