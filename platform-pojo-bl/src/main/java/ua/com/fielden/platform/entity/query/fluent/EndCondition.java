package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndCondition;

abstract class EndCondition<T> //
		extends AbstractQueryLink //
		implements IEndCondition<T> {

    protected EndCondition(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForEndCondition(final Tokens tokens);

	@Override
	public T end() {
		return nextForEndCondition(getTokens().endCondition());
	}
}