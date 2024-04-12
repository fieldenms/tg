package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IArithmeticalOperator;

abstract class ArithmeticalOperator<T> //
		extends AbstractQueryLink //
		implements IArithmeticalOperator<T> {

	protected ArithmeticalOperator(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForArithmeticalOperator(final EqlSentenceBuilder builder);

	@Override
	public T add() {
		return nextForArithmeticalOperator(builder.add());
	}

	@Override
	public T sub() {
		return nextForArithmeticalOperator(builder.subtract());
	}

	@Override
	public T mult() {
		return nextForArithmeticalOperator(builder.multiply());
	}

	@Override
	public T div() {
		return nextForArithmeticalOperator(builder.divide());
	}

	@Override
	public T mod() {
		return nextForArithmeticalOperator(builder.modulo());
	}

}
