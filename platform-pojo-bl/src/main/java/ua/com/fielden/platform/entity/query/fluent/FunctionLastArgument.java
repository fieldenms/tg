package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

final class FunctionLastArgument<T> extends AbstractExprOperand<T, IExprOperand0<T>> implements IFunctionLastArgument<T> {
    T parent;
    FunctionLastArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<T> getParent2() {
	return new ExprOperand0<T>(getTokens(), parent);
    }

    @Override
    T getParent() {
	return parent;
    }
}
