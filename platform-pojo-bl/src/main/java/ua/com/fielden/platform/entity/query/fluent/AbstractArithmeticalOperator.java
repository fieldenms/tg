package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IArithmeticalOperator;


abstract class AbstractArithmeticalOperator<T> extends AbstractQueryLink implements IArithmeticalOperator<T> {
    abstract T getParent();

    protected AbstractArithmeticalOperator(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T add() {
	getTokens().add();
	return getParent();
    }

    @Override
    public T sub() {
	getTokens().subtract();
	return getParent();
    }

    @Override
    public T mult() {
	getTokens().multiply();
	return getParent();
    }

    @Override
    public T div() {
	getTokens().divide();
	return getParent();
    }
}
