package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;

final class YieldExprItem2<T> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd2<T>, IYieldExprItem3<T>> implements IYieldExprItem2<T> {
    T parent;

    YieldExprItem2(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprItem3<T> getParent2() {
	return new YieldExprItem3<T>(getTokens(), parent);
    }

    @Override
    IYieldExprOperationOrEnd2<T> getParent() {
	return new YieldExprOperationOrEnd2<T>(getTokens(), parent);
    }
}
