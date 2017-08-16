package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class AbstractLogicalCondition<T> //
		extends AbstractQueryLink //
		implements ILogicalOperator<T> {

	protected abstract T nextForAbstractLogicalCondition();

	@Override
	public T and() {
		return copy(nextForAbstractLogicalCondition(), getTokens().and());
	}

	@Override
	public T or() {
		return copy(nextForAbstractLogicalCondition(), getTokens().or());
	}
}