package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class AbstractLogicalCondition<T> extends AbstractQueryLink implements ILogicalOperator<T> {
    abstract T getParent();

    protected AbstractLogicalCondition(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public T and() {
        return copy(getParent(), getTokens().and());
    }

    @Override
    public T or() {
        return copy(getParent(), getTokens().or());
    }
}