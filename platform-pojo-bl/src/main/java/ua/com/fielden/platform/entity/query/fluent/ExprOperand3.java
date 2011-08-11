package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

final class ExprOperand3<T> extends AbstractSingleOperand<IExprOperationOrEnd3<T>> implements IExprOperand3<T> {
    T parent;
    ExprOperand3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd3<T> getParent() {
	return new ExprOperationOrEnd3<T>(getTokens(), parent);
    }
}
