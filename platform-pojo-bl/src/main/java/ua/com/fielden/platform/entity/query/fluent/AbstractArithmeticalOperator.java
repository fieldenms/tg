package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IArithmeticalOperator;


abstract class AbstractArithmeticalOperator<T> extends AbstractQueryLink implements IArithmeticalOperator<T> {
    abstract T getParent();

    protected AbstractArithmeticalOperator(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T add() {
	return copy(getParent(), getTokens().add());
    }

    @Override
    public T sub() {
	return copy(getParent(), getTokens().subtract());
    }

    @Override
    public T mult() {
	return copy(getParent(), getTokens().multiply());
    }

    @Override
    public T div() {
	return copy(getParent(), getTokens().divide());
    }
}