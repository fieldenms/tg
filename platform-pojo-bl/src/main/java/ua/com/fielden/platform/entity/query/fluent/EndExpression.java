package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndExpression;

abstract class EndExpression<T> //
        extends AbstractQueryLink //
        implements IEndExpression<T> {

    protected EndExpression(final EqlSentenceBuilder builder) {
        super(builder);
    }

    protected abstract T nextForEndExpression(final EqlSentenceBuilder builder);

    @Override
    public T endExpr() {
        return nextForEndExpression(builder.endExpression());
    }

}
