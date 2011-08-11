package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

class ExprOperationOrEnd0<T> extends AbstractExprOperationOrEnd<IExprOperand0<T>, T> implements IExprOperationOrEnd0<T> {
    T parent;
    ExprOperationOrEnd0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<T> getParent() {
	return new ExprOperand0<T>(getTokens(), parent);
    }

    @Override
    T getParent2() {
	return parent;
    }
}
