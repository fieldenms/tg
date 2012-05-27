package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

final class ExprOperand0<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IExprOperationOrEnd0<T, ET>, IExprOperand1<T, ET>, ET> implements IExprOperand0<T, ET> {
    T parent;

    ExprOperand0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd0<T, ET> getParent() {
	return new ExprOperationOrEnd0<T, ET>(getTokens(), parent);
    }

    @Override
    IExprOperand1<T, ET> getParent2() {
	return new ExprOperand1<T, ET>(getTokens(), parent);
    }
}
