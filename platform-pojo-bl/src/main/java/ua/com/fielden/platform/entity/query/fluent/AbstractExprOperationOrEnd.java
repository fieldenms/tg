package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd;


abstract class AbstractExprOperationOrEnd<T1, T2, ET extends AbstractEntity<?>> extends AbstractArithmeticalOperator<T1> implements IExprOperationOrEnd<T1, T2, ET> {
    abstract T2 getParent2();

    protected AbstractExprOperationOrEnd(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public T2 endExpr() {
	return copy(getParent2(), getTokens().endExpression());
    }
}
