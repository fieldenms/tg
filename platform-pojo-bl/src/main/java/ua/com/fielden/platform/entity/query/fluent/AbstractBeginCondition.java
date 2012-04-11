package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IBeginCondition;


abstract class AbstractBeginCondition<T> extends AbstractQueryLink implements IBeginCondition<T> {
    abstract T getParent();

    protected AbstractBeginCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T begin() {
	return copy(getParent(), getTokens().beginCondition(false));
    }

    @Override
    public T notBegin() {
	return copy(getParent(), getTokens().beginCondition(true));
    }
}
