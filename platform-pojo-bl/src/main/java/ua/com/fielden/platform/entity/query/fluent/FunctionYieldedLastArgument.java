package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

public class FunctionYieldedLastArgument<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<T, IYieldExprItem0<T, ET>, ET> implements IFunctionYieldedLastArgument<T, ET> {
    T parent;

    protected FunctionYieldedLastArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprItem0<T, ET> getParent2() {
	return new YieldExprItem0<T, ET>(getTokens(), parent);
    }

    @Override
    T getParent() {
	return parent;
    }
}