package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndCondition;

abstract class AbstractEndCondition<T> extends AbstractQueryLink implements IEndCondition<T> {
    abstract T nextForAbstractEndCondition();

    @Override
    public T end() {
        return copy(nextForAbstractEndCondition(), getTokens().endCondition());
    }
}