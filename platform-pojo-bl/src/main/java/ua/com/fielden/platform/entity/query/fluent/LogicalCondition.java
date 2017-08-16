package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class LogicalCondition<T> //
		extends AbstractQueryLink //
		implements ILogicalOperator<T> {

	protected abstract T nextForLogicalCondition();

	@Override
	public T and() {
		return copy(nextForLogicalCondition(), getTokens().and());
	}

	@Override
	public T or() {
		return copy(nextForLogicalCondition(), getTokens().or());
	}
}