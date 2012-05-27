package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

class YieldExprOperationOrEnd3<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IYieldExprItem3<T, ET>, IYieldExprOperationOrEnd2<T, ET>, ET> implements IYieldExprOperationOrEnd3<T, ET> {
    T parent;
    YieldExprOperationOrEnd3(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }
    @Override
    IYieldExprOperationOrEnd2<T, ET> getParent2() {
	return new YieldExprOperationOrEnd2<T, ET>(getTokens(), parent);
    }
    @Override
    IYieldExprItem3<T, ET> getParent() {
	return new YieldExprItem3<T, ET>(getTokens(), parent);
    }
}