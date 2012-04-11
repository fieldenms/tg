package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IBeginCondition;


abstract class AbstractBeginCondition<T> extends AbstractQueryLink implements IBeginCondition<T> {
    abstract T getParent();

    protected AbstractBeginCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T begin() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().beginCondition(false));
	return result;
    }

    @Override
    public T notBegin() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().beginCondition(true));
	return result;
    }
}
