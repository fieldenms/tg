package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperand;

abstract class AbstractYieldedItem<T> extends AbstractSingleOperand<T> implements IYieldOperand<T> {
    protected AbstractYieldedItem(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IFunctionLastArgument<T> maxOf() {
	return new FunctionLastArgument<T>(getTokens().maxOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> minOf() {
	return new FunctionLastArgument<T>(getTokens().minOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> sumOf() {
	return new FunctionLastArgument<T>(getTokens().sumOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> countOf() {
	return new FunctionLastArgument<T>(getTokens().countOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> avgOf() {
	return new FunctionLastArgument<T>(getTokens().averageOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> sumOfDistinct() {
	return new FunctionLastArgument<T>(getTokens().sumOfDistinct(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> countOfDistinct() {
	return new FunctionLastArgument<T>(getTokens().countOfDistinct(), getParent());
    }

    @Override
    public IFunctionLastArgument<T> avgOfDistinct() {
	return new FunctionLastArgument<T>(getTokens().averageOfDistinct(), getParent());
    }

    @Override
    public T countAll() {
	getTokens().countAll();
	return getParent();
    }

    @Override
    public T join(final String joinAlias) {
	getTokens().entity(joinAlias);
	return getParent();
    }
}
