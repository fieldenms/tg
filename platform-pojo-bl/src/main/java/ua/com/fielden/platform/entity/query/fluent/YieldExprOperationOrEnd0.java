package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

class YieldExprOperationOrEnd0<T> extends AbstractExprOperationOrEnd<IYieldExprItem0<T>, T> implements IYieldExprOperationOrEnd0<T> {
    T parent;
    YieldExprOperationOrEnd0(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }
    @Override
    T getParent2() {
	return parent;
    }
    @Override
    IYieldExprItem0<T> getParent() {
	return new YieldExprItem0<T>(getTokens(), parent);
    }
}
