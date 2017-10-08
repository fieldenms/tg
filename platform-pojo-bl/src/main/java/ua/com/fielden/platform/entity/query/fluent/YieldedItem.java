package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldOperand;

abstract class YieldedItem<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<T, ET> //
		implements IYieldOperand<T, ET> {

    public YieldedItem(final Tokens tokens) {
        super(tokens);
    }
    
	@Override
	public IFunctionLastArgument<T, ET> maxOf() {
		return createFunctionLastArgument(getTokens().maxOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> minOf() {
		return createFunctionLastArgument(getTokens().minOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> sumOf() {
		return createFunctionLastArgument(getTokens().sumOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> countOf() {
		return createFunctionLastArgument(getTokens().countOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> avgOf() {
		return createFunctionLastArgument(getTokens().averageOf());
	}

	@Override
	public IFunctionLastArgument<T, ET> sumOfDistinct() {
		return createFunctionLastArgument(getTokens().sumOfDistinct());
	}

	@Override
	public IFunctionLastArgument<T, ET> countOfDistinct() {
		return createFunctionLastArgument(getTokens().countOfDistinct());
	}

	@Override
	public IFunctionLastArgument<T, ET> avgOfDistinct() {
		return createFunctionLastArgument(getTokens().averageOfDistinct());
	}

	@Override
	public T countAll() {
		return nextForSingleOperand(getTokens().countAll());
	}
}