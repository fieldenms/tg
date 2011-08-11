package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IBeginCondition;


abstract class AbstractBeginCondition<T> extends AbstractQueryLink implements IBeginCondition<T> {
    abstract T getParent();

    protected AbstractBeginCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T begin() {
	getTokens().beginCondition(false);
	return getParent();
    }

    @Override
    public T notBegin() {
	getTokens().beginCondition(true);
	return getParent();
    }
}
