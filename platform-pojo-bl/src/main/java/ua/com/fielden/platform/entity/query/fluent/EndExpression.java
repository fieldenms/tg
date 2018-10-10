package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndExpression;

abstract class EndExpression<T> //
		extends AbstractQueryLink //
		implements IEndExpression<T> {

    protected EndExpression(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForEndExpression(final Tokens tokens);

	@Override
	public T endExpr() {
		return nextForEndExpression(getTokens().endExpression());
	}
}