package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndCondition;

abstract class AbstractEndCondition<T> extends AbstractQueryLink implements IEndCondition<T> {
    abstract T getParent();

    @Override
    public T end() {
        return copy(getParent(), getTokens().endCondition());
    }
}