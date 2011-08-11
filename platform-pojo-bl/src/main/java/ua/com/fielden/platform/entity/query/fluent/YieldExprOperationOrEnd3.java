package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;

class YieldExprOperationOrEnd3<T> extends AbstractExprOperationOrEnd<IYieldExprItem3<T>, IYieldExprOperationOrEnd2<T>> implements IYieldExprOperationOrEnd3<T> {
    T parent;
    YieldExprOperationOrEnd3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }
    @Override
    IYieldExprOperationOrEnd2<T> getParent2() {
	return new YieldExprOperationOrEnd2<T>(getTokens(), parent);
    }
    @Override
    IYieldExprItem3<T> getParent() {
	return new YieldExprItem3<T>(getTokens(), parent);
    }
}