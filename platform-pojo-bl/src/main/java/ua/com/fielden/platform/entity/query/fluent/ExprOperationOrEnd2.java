package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

final class ExprOperationOrEnd2<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IExprOperand2<T, ET>, IExprOperationOrEnd1<T, ET>, ET> implements IExprOperationOrEnd2<T, ET> {
    T parent;
    ExprOperationOrEnd2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd1<T, ET> getParent2() {
	return new ExprOperationOrEnd1<T, ET>(getTokens(), parent);
    }

    @Override
    IExprOperand2<T, ET> getParent() {
	return new ExprOperand2<T, ET>(getTokens(), parent);
    }
}
