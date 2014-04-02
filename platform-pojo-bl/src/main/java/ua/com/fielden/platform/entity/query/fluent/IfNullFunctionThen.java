package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

final class IfNullFunctionThen<T, ET extends AbstractEntity<?>> extends AbstractQueryLink implements IIfNullFunctionThen<T, ET> {
    T parent;

    IfNullFunctionThen(final Tokens queryTokens, final T parent) {
        super(queryTokens);
        this.parent = parent;
    }

    @Override
    public IFunctionLastArgument<T, ET> then() {
        return new FunctionLastArgument<T, ET>(this.getTokens(), parent);
    }
}