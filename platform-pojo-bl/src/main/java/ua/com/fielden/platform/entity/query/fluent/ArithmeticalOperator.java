package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IArithmeticalOperator;

abstract class ArithmeticalOperator<T> //
		extends AbstractQueryLink //
		implements IArithmeticalOperator<T> {

    protected ArithmeticalOperator(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForArithmeticalOperator(final Tokens tokens);

	@Override
	public T add() {
		return nextForArithmeticalOperator(getTokens().add());
	}

	@Override
	public T sub() {
		return nextForArithmeticalOperator(getTokens().subtract());
	}

	@Override
	public T mult() {
		return nextForArithmeticalOperator(getTokens().multiply());
	}

	@Override
	public T div() {
		return nextForArithmeticalOperator(getTokens().divide());
	}

	@Override
	public T mod() {
		return nextForArithmeticalOperator(getTokens().modulo());
	}
}