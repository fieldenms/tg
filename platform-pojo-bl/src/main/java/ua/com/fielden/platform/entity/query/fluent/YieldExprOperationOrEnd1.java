package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;

class YieldExprOperationOrEnd1<T> extends AbstractExprOperationOrEnd<IYieldExprItem1<T>, IYieldExprOperationOrEnd0<T>> implements IYieldExprOperationOrEnd1<T> {
    T parent;
    YieldExprOperationOrEnd1(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }
    @Override
    IYieldExprOperationOrEnd0<T> getParent2() {
	return new YieldExprOperationOrEnd0<T>(getTokens(), parent);
    }
    @Override
    IYieldExprItem1<T> getParent() {
	return new YieldExprItem1<T>(getTokens(), parent);
    }
}