package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class AbstractLogicalCondition<T> extends AbstractQueryLink implements ILogicalOperator<T> {
    abstract T getParent();

    protected AbstractLogicalCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T and() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().and());
	return result;
    }

    @Override
    public T or() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().or());
	return result;
    }
}
