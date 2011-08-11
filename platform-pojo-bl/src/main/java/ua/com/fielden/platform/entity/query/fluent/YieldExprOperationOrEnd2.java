package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;

class YieldExprOperationOrEnd2<T> extends AbstractExprOperationOrEnd<IYieldExprItem2<T>, IYieldExprOperationOrEnd1<T>> implements IYieldExprOperationOrEnd2<T> {
    T parent;
    YieldExprOperationOrEnd2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }
    @Override
    IYieldExprOperationOrEnd1<T> getParent2() {
	return new YieldExprOperationOrEnd1<T>(getTokens(), parent);
    }
    @Override
    IYieldExprItem2<T> getParent() {
	return new YieldExprItem2<T>(getTokens(), parent);
    }
}