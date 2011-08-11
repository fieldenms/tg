package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class CaseWhenFunctionArgument<T> extends AbstractExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>>> implements ICaseWhenFunctionArgument<T> {
    T parent;
    CaseWhenFunctionArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<ICaseWhenFunctionEnd<T>> getParent2() {
	return new ExprOperand0<ICaseWhenFunctionEnd<T>>(getTokens(), new CaseWhenFunctionEnd<T>(getTokens(), parent));
    }

    @Override
    ICaseWhenFunctionEnd<T> getParent() {
	return new CaseWhenFunctionEnd<T>(getTokens(), parent);
    }
}
