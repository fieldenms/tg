package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IArithmeticalOperator;


abstract class AbstractArithmeticalOperator<T> extends AbstractQueryLink implements IArithmeticalOperator<T> {
    abstract T getParent();

    protected AbstractArithmeticalOperator(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T add() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().add());
	return result;
    }

    @Override
    public T sub() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().subtract());
	return result;
    }

    @Override
    public T mult() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().multiply());
	return result;
    }

    @Override
    public T div() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().divide());
	return result;
    }
}
