package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionYieldedLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;

abstract class FunctionYieldedLastArgument<T, ET extends AbstractEntity<?>> //
		extends YieldExprOperand<T, IYieldExprItem0<T, ET>, ET> //
		implements IFunctionYieldedLastArgument<T, ET> {

	protected abstract T nextForFunctionYieldedLastArgument();

	@Override
	protected IYieldExprItem0<T, ET> nextForAbstractYieldExprOperand() {
		return new YieldExprItem0<T, ET>() {

			@Override
			protected T nextForYieldExprItem0() {
				return FunctionYieldedLastArgument.this.nextForFunctionYieldedLastArgument();
			}

		};
	}

	@Override
	protected T nextForAbstractSingleOperand() {
		return nextForFunctionYieldedLastArgument();
	}
}