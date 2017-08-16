package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class FunctionLastArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<T, IExprOperand0<T, ET>, ET> //
		implements IFunctionLastArgument<T, ET> {

	protected abstract T nextForFunctionLastArgument();

	@Override
	protected IExprOperand0<T, ET> nextForAbstractExprOperand() {
		return new ExprOperand0<T, ET>() {

			@Override
			protected T nextForExprOperand0() {
				return FunctionLastArgument.this.nextForFunctionLastArgument();
			}

		};
	}

	@Override
	protected T nextForAbstractSingleOperand() {
		return nextForFunctionLastArgument();
	}
}