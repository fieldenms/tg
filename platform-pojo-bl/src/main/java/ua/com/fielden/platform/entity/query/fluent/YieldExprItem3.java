package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

final class YieldExprItem3<T, ET extends AbstractEntity<?>> extends AbstractYieldedItem<IYieldExprOperationOrEnd3<T, ET>, ET> implements IYieldExprItem3<T, ET> {
    T parent;

    YieldExprItem3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprOperationOrEnd3<T, ET> getParent() {
	return new YieldExprOperationOrEnd3<T, ET>(getTokens(), parent);
    }
}