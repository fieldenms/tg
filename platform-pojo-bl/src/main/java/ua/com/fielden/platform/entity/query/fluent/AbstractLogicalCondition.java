package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class AbstractLogicalCondition<T> extends AbstractQueryLink implements ILogicalOperator<T> {
    abstract T getParent();

    protected AbstractLogicalCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T and() {
	getTokens().and();
	return getParent();
    }

    @Override
    public T or() {
	getTokens().or();
	return getParent();
    }
}
