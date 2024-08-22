package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ILogicalOperator;

abstract class LogicalCondition<T> //
        extends AbstractQueryLink //
        implements ILogicalOperator<T> {

    protected LogicalCondition(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForLogicalCondition(final EqlSentenceBuilder builder);

    @Override
    public T and() {
        return nextForLogicalCondition(builder.and());
    }

    @Override
    public T or() {
        return nextForLogicalCondition(builder.or());
    }

}
