package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndExpression;

abstract class EndExpression<T> //
		extends AbstractQueryLink //
		implements IEndExpression<T> {

	protected abstract T nextForAbstractEndExpression();

	@Override
	public T endExpr() {
		return copy(nextForAbstractEndExpression(), getTokens().endExpression());
	}
}