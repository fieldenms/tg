package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperand;

abstract class AbstractYieldedItem<T, ET extends AbstractEntity<?>> extends AbstractSingleOperand<T, ET> implements IYieldOperand<T, ET> {

	@Override
    public IFunctionLastArgument<T, ET> maxOf() {
    	return copy(createFunctionLastArgument(), getTokens().maxOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> minOf() {
    	return copy(createFunctionLastArgument(), getTokens().minOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> sumOf() {
        return copy(createFunctionLastArgument(), getTokens().sumOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> countOf() {
        return copy(createFunctionLastArgument(), getTokens().countOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> avgOf() {
    	return copy(createFunctionLastArgument(), getTokens().averageOf());
    }

    @Override
    public IFunctionLastArgument<T, ET> sumOfDistinct() {
        return copy(createFunctionLastArgument(), getTokens().sumOfDistinct());
    }

    @Override
    public IFunctionLastArgument<T, ET> countOfDistinct() {
        return copy(createFunctionLastArgument(), getTokens().countOfDistinct());
    }

    @Override
    public IFunctionLastArgument<T, ET> avgOfDistinct() {
        return copy(createFunctionLastArgument(), getTokens().averageOfDistinct());
    }

    @Override
    public T countAll() {
        return copy(nextForAbstractSingleOperand(), getTokens().countAll());
    }
}