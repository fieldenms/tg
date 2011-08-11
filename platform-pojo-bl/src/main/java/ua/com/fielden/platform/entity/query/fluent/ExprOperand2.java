package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

final class ExprOperand2<T> extends AbstractExprOperand<IExprOperationOrEnd2<T>, IExprOperand3<T>> implements IExprOperand2<T> {
    T parent;
    ExprOperand2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand3<T> getParent2() {
	return new ExprOperand3<T>(getTokens(), parent);
    }

    @Override
    IExprOperationOrEnd2<T> getParent() {
	return new ExprOperationOrEnd2<T>(getTokens(), parent);
    }
}
