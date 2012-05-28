package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

final class FunctionLastArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<T, IExprOperand0<T, ET>, ET> implements IFunctionLastArgument<T, ET> {
    T parent;
    FunctionLastArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<T, ET> getParent2() {
	return new ExprOperand0<T, ET>(getTokens(), parent);
    }

    @Override
    T getParent() {
	return parent;
    }
}