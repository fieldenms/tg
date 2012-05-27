package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

class ExprOperationOrEnd1<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IExprOperand1<T, ET>, IExprOperationOrEnd0<T, ET>, ET> implements IExprOperationOrEnd1<T, ET> {
    T parent;
    ExprOperationOrEnd1(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd0<T, ET> getParent2() {
	return new ExprOperationOrEnd0<T, ET>(getTokens(), parent);
    }

    @Override
    IExprOperand1<T, ET> getParent() {
	return new ExprOperand1<T, ET>(getTokens(), parent);
    }
}