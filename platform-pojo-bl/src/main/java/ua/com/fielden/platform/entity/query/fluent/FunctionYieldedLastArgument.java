package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

public class FunctionYieldedLastArgument<T> extends AbstractYieldExprOperand<T, IYieldExprItem0<T>> implements IFunctionYieldedLastArgument<T> {
    T parent;

    protected FunctionYieldedLastArgument(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }

    @Override
    IYieldExprItem0<T> getParent2() {
	return new YieldExprItem0<T>(getTokens(), parent);
    }

    @Override
    T getParent() {
	return parent;
    }
}
