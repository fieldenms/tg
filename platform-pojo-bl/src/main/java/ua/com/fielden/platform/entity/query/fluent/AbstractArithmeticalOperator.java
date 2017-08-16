package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IArithmeticalOperator;

abstract class AbstractArithmeticalOperator<T> //
		extends AbstractQueryLink //
		implements IArithmeticalOperator<T> {

	protected abstract T nextForAbstractArithmeticalOperator();

	@Override
	public T add() {
		return copy(nextForAbstractArithmeticalOperator(), getTokens().add());
	}

	@Override
	public T sub() {
		return copy(nextForAbstractArithmeticalOperator(), getTokens().subtract());
	}

	@Override
	public T mult() {
		return copy(nextForAbstractArithmeticalOperator(), getTokens().multiply());
	}

	@Override
	public T div() {
		return copy(nextForAbstractArithmeticalOperator(), getTokens().divide());
	}

	@Override
	public T mod() {
		return copy(nextForAbstractArithmeticalOperator(), getTokens().modulo());
	}
}