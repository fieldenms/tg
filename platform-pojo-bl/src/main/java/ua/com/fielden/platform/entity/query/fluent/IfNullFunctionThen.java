package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

final class IfNullFunctionThen<T> extends AbstractQueryLink implements IIfNullFunctionThen<T> {
    T parent;

    IfNullFunctionThen(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    public IFunctionLastArgument<T> then() {
	return new FunctionLastArgument<T>(this.getTokens(), parent);
    }
}
