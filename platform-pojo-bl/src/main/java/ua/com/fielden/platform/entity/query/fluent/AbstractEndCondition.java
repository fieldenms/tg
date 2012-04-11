package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndCondition;


abstract class AbstractEndCondition<T> extends AbstractQueryLink implements IEndCondition<T> {
    abstract T getParent();

    protected AbstractEndCondition(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T end() {
	final T result = getParent();
	((AbstractQueryLink) result).setTokens(getTokens().endCondition());
	return result;
    }
}
