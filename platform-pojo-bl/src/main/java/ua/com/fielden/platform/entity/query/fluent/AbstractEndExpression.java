package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IEndExpression;

abstract class AbstractEndExpression<T> extends AbstractQueryLink implements IEndExpression<T> {

    abstract T getParent();

    protected AbstractEndExpression(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public T endExpr() {
        return copy(getParent(), getTokens().endExpression());
    }
}