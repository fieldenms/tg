package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

final class ExprOperand1<T> extends AbstractExprOperand<IExprOperationOrEnd1<T>, IExprOperand2<T>> implements IExprOperand1<T> {
    T parent;

    ExprOperand1(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand2<T> getParent2() {
	return new ExprOperand2<T>(getTokens(), parent);
    }

    @Override
    IExprOperationOrEnd1<T> getParent() {
	return new ExprOperationOrEnd1<T>(getTokens(), parent);
    }
}
