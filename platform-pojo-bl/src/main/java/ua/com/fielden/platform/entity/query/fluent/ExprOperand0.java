package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

final class ExprOperand0<T> extends AbstractExprOperand<IExprOperationOrEnd0<T>, IExprOperand1<T>> implements IExprOperand0<T> {
    T parent;

    ExprOperand0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd0<T> getParent() {
	return new ExprOperationOrEnd0<T>(getTokens(), parent);
    }

    @Override
    IExprOperand1<T> getParent2() {
	return new ExprOperand1<T>(getTokens(), parent);
    }
}
