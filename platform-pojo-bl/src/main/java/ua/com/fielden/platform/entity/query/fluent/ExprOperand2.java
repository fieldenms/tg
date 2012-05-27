package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

final class ExprOperand2<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IExprOperationOrEnd2<T, ET>, IExprOperand3<T, ET>, ET> implements IExprOperand2<T, ET> {
    T parent;
    ExprOperand2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperand3<T, ET> getParent2() {
	return new ExprOperand3<T, ET>(getTokens(), parent);
    }

    @Override
    IExprOperationOrEnd2<T, ET> getParent() {
	return new ExprOperationOrEnd2<T, ET>(getTokens(), parent);
    }
}
