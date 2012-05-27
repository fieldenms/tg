package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

class YieldExprOperationOrEnd0<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IYieldExprItem0<T, ET>, T, ET> implements IYieldExprOperationOrEnd0<T, ET> {
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
    IYieldExprItem0<T, ET> getParent() {
	return new YieldExprItem0<T, ET>(getTokens(), parent);
    }
}