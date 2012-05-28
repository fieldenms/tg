package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

final class IfNullFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IIfNullFunctionThen<T, ET>, IExprOperand0<IIfNullFunctionThen<T, ET>, ET>, ET> implements IIfNullFunctionArgument<T, ET> {
    T parent;

    IfNullFunctionArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<IIfNullFunctionThen<T, ET>, ET> getParent2() {
	return new ExprOperand0<IIfNullFunctionThen<T, ET>, ET>(getTokens(), new IfNullFunctionThen<T, ET>(getTokens(), parent));
    }

    @Override
    IIfNullFunctionThen<T, ET> getParent() {
	return new IfNullFunctionThen<T, ET>(getTokens(), parent);
    }
}