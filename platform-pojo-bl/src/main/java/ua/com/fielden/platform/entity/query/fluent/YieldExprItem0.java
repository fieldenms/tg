package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

final class YieldExprItem0<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd0<T, ET>, IYieldExprItem1<T, ET>, ET> implements IYieldExprItem0<T, ET> {
    T parent;

    YieldExprItem0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprItem1<T, ET> getParent2() {
	return new YieldExprItem1<T, ET>(getTokens(), parent);
    }

    @Override
    IYieldExprOperationOrEnd0<T, ET> getParent() {
	return new YieldExprOperationOrEnd0<T, ET>(getTokens(), parent);
    }
}