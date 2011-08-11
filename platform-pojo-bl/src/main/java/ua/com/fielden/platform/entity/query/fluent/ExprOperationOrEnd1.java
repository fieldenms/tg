package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

class ExprOperationOrEnd1<T> extends AbstractExprOperationOrEnd<IExprOperand1<T>, IExprOperationOrEnd0<T>> implements IExprOperationOrEnd1<T> {
    T parent;
    ExprOperationOrEnd1(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd0<T> getParent2() {
	return new ExprOperationOrEnd0<T>(getTokens(), parent);
    }

    @Override
    IExprOperand1<T> getParent() {
	return new ExprOperand1<T>(getTokens(), parent);
    }
}