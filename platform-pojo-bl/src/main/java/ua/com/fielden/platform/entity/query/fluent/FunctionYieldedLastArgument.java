package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

abstract class FunctionYieldedLastArgument<T, ET extends AbstractEntity<?>> //
		extends YieldExprOperand<T, IYieldExprItem0<T, ET>, ET> //
		implements IFunctionYieldedLastArgument<T, ET> {

    protected FunctionYieldedLastArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForFunctionYieldedLastArgument(final Tokens tokens);

	@Override
	protected IYieldExprItem0<T, ET> nextForYieldExprOperand(final Tokens tokens) {
		return new YieldExprItem0<T, ET>(tokens) {

			@Override
			protected T nextForYieldExprItem0(final Tokens tokens) {
				return FunctionYieldedLastArgument.this.nextForFunctionYieldedLastArgument(tokens);
			}

		};
	}

	@Override
	protected T nextForSingleOperand(final Tokens tokens) {
		return nextForFunctionYieldedLastArgument(tokens);
	}
}