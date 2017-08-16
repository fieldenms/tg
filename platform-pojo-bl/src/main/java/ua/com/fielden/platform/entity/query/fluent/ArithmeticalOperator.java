package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IArithmeticalOperator;

abstract class ArithmeticalOperator<T> //
		extends AbstractQueryLink //
		implements IArithmeticalOperator<T> {

	protected abstract T nextForArithmeticalOperator();

	@Override
	public T add() {
		return copy(nextForArithmeticalOperator(), getTokens().add());
	}

	@Override
	public T sub() {
		return copy(nextForArithmeticalOperator(), getTokens().subtract());
	}

	@Override
	public T mult() {
		return copy(nextForArithmeticalOperator(), getTokens().multiply());
	}

	@Override
	public T div() {
		return copy(nextForArithmeticalOperator(), getTokens().divide());
	}

	@Override
	public T mod() {
		return copy(nextForArithmeticalOperator(), getTokens().modulo());
	}
}