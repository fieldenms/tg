package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class LogicalCondition<T> //
		extends AbstractQueryLink //
		implements ILogicalOperator<T> {

    protected LogicalCondition(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForLogicalCondition(final Tokens tokens);

	@Override
	public T and() {
		return nextForLogicalCondition(getTokens().and());
	}

	@Override
	public T or() {
		return nextForLogicalCondition(getTokens().or());
	}
}