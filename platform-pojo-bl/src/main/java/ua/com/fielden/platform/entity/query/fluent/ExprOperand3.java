package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

final class ExprOperand3<T, ET extends AbstractEntity<?>> extends AbstractSingleOperand<IExprOperationOrEnd3<T, ET>, ET> implements IExprOperand3<T, ET> {
    T parent;
    ExprOperand3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IExprOperationOrEnd3<T, ET> getParent() {
	return new ExprOperationOrEnd3<T, ET>(getTokens(), parent);
    }
}