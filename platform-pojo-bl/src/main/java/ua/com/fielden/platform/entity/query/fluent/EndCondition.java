package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndCondition;

abstract class EndCondition<T> //
		extends AbstractQueryLink //
		implements IEndCondition<T> {

	protected abstract T nextForAbstractEndCondition();

	@Override
	public T end() {
		return copy(nextForAbstractEndCondition(), getTokens().endCondition());
	}
}