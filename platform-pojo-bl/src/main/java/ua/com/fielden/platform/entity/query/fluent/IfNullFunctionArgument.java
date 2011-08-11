package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

final class IfNullFunctionArgument<T> extends AbstractExprOperand<IIfNullFunctionThen<T>, IExprOperand0<IIfNullFunctionThen<T>>> implements IIfNullFunctionArgument<T> {
    T parent;
    IfNullFunctionArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<IIfNullFunctionThen<T>> getParent2() {
	return new ExprOperand0<IIfNullFunctionThen<T>>(getTokens(), new IfNullFunctionThen<T>(getTokens(), parent));
    }

    @Override
    IIfNullFunctionThen<T> getParent() {
	return new IfNullFunctionThen<T>(getTokens(), parent);
    }
}
