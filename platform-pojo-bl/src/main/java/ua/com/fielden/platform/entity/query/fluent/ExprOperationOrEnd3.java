package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

final class ExprOperationOrEnd3<T> extends AbstractExprOperationOrEnd<IExprOperand3<T>, IExprOperationOrEnd2<T>> implements IExprOperationOrEnd3<T> {
    T parent;
    ExprOperationOrEnd3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd2<T> getParent2() {
	return new ExprOperationOrEnd2<T>(getTokens(), parent);
    }

    @Override
    IExprOperand3<T> getParent() {
	return new ExprOperand3<T>(getTokens(), parent);
    }
}
