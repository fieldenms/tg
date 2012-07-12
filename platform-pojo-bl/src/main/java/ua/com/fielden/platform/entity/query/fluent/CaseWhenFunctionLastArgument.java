package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

final class CaseWhenFunctionLastArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET> implements ICaseWhenFunctionLastArgument<T, ET> {
    T parent;

    CaseWhenFunctionLastArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand0<ICaseWhenFunctionEnd<T>, ET> getParent2() {
	return new ExprOperand0<ICaseWhenFunctionEnd<T>, ET>(getTokens(), new CaseWhenFunctionEnd<T>(getTokens(), parent));
    }

    @Override
    ICaseWhenFunctionWhen<T, ET> getParent() {
	return new CaseWhenFunctionWhen<T, ET>(getTokens(), parent);
    }
}