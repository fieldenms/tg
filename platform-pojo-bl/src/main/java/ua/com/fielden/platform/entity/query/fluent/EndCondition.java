package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndCondition;

abstract class EndCondition<T> //
		extends AbstractQueryLink //
		implements IEndCondition<T> {

	protected abstract T nextForEndCondition();

	@Override
	public T end() {
		return copy(nextForEndCondition(), getTokens().endCondition());
	}
}