package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;

final class YieldExprItem3<T> extends AbstractYieldedItem<IYieldExprOperationOrEnd3<T>> implements IYieldExprItem3<T> {
    T parent;

    YieldExprItem3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprOperationOrEnd3<T> getParent() {
	return new YieldExprOperationOrEnd3<T>(getTokens(), parent);
    }
}
