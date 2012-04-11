package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd;


abstract class AbstractExprOperationOrEnd<T1, T2> extends AbstractArithmeticalOperator<T1> implements IExprOperationOrEnd<T1, T2> {
    abstract T2 getParent2();

    protected AbstractExprOperationOrEnd(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T2 endExpr() {
	return copy(getParent2(), getTokens().endExpression());
    }
}
