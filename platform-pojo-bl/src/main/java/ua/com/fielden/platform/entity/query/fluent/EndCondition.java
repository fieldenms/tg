package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndCondition;

abstract class EndCondition<T> //
        extends AbstractQueryLink //
        implements IEndCondition<T> {

    protected EndCondition(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForEndCondition(final EqlSentenceBuilder builder);

    @Override
    public T end() {
        return nextForEndCondition(builder.endCondition());
    }

}
