package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

final class ExprOperationOrEnd2<T> extends AbstractExprOperationOrEnd<IExprOperand2<T>, IExprOperationOrEnd1<T>> implements IExprOperationOrEnd2<T> {
    T parent;
    ExprOperationOrEnd2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd1<T> getParent2() {
	return new ExprOperationOrEnd1<T>(getTokens(), parent);
    }

    @Override
    IExprOperand2<T> getParent() {
	return new ExprOperand2<T>(getTokens(), parent);
    }
}
