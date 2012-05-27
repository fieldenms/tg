package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class CaseWhenFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET> implements ICaseWhenFunctionArgument<T, ET> {
    T parent;
    CaseWhenFunctionArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<ICaseWhenFunctionEnd<T>, ET> getParent2() {
	return new ExprOperand0<ICaseWhenFunctionEnd<T>, ET>(getTokens(), new CaseWhenFunctionEnd<T>(getTokens(), parent));
    }

    @Override
    ICaseWhenFunctionEnd<T> getParent() {
	return new CaseWhenFunctionEnd<T>(getTokens(), parent);
    }
}
