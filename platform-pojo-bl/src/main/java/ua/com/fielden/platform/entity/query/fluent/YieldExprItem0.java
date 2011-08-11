package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;

final class YieldExprItem0<T> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd0<T>, IYieldExprItem1<T>> implements IYieldExprItem0<T> {
    T parent;

    YieldExprItem0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprItem1<T> getParent2() {
	return new YieldExprItem1<T>(getTokens(), parent);
    }

    @Override
    IYieldExprOperationOrEnd0<T> getParent() {
	return new YieldExprOperationOrEnd0<T>(getTokens(), parent);
    }
}
