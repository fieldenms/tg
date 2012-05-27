package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperand;

abstract class AbstractYieldedItem<T, ET extends AbstractEntity<?>> extends AbstractSingleOperand<T, ET> implements IYieldOperand<T, ET> {
    protected AbstractYieldedItem(final Tokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public IFunctionLastArgument<T, ET> maxOf() {
	return new FunctionLastArgument<T, ET>(getTokens().maxOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> minOf() {
	return new FunctionLastArgument<T, ET>(getTokens().minOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> sumOf() {
	return new FunctionLastArgument<T, ET>(getTokens().sumOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> countOf() {
	return new FunctionLastArgument<T, ET>(getTokens().countOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> avgOf() {
	return new FunctionLastArgument<T, ET>(getTokens().averageOf(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> sumOfDistinct() {
	return new FunctionLastArgument<T, ET>(getTokens().sumOfDistinct(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> countOfDistinct() {
	return new FunctionLastArgument<T, ET>(getTokens().countOfDistinct(), getParent());
    }

    @Override
    public IFunctionLastArgument<T, ET> avgOfDistinct() {
	return new FunctionLastArgument<T, ET>(getTokens().averageOfDistinct(), getParent());
    }

    @Override
    public T countAll() {
	return copy(getParent(), getTokens().countAll());
    }

    @Override
    public T join(final String joinAlias) {
	return copy(getParent(), getTokens().entity(joinAlias));
    }
}