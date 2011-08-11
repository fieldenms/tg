package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;

final class YieldExprItem1<T> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd1<T>, IYieldExprItem2<T>> implements IYieldExprItem1<T> {
    T parent;

    YieldExprItem1(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprItem2<T> getParent2() {
	return new YieldExprItem2<T>(getTokens(), parent);
    }

    @Override
    IYieldExprOperationOrEnd1<T> getParent() {
	return new YieldExprOperationOrEnd1<T>(getTokens(), parent);
    }
}
