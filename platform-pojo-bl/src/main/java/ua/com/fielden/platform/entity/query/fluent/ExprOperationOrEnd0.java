package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

class ExprOperationOrEnd0<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IExprOperand0<T, ET>, T, ET> implements IExprOperationOrEnd0<T, ET> {
    T parent;
    ExprOperationOrEnd0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<T, ET> getParent() {
	return new ExprOperand0<T, ET>(getTokens(), parent);
    }

    @Override
    T getParent2() {
	return parent;
    }
}