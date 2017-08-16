package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

abstract class IfNullFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IIfNullFunctionThen<T, ET>, IExprOperand0<IIfNullFunctionThen<T, ET>, ET>, ET> //
		implements IIfNullFunctionArgument<T, ET> {

	protected abstract T nextForIfNullFunctionArgument();

	@Override
	protected IExprOperand0<IIfNullFunctionThen<T, ET>, ET> nextForExprOperand() {
		return new ExprOperand0<IIfNullFunctionThen<T, ET>, ET>() {

			@Override
			protected IIfNullFunctionThen<T, ET> nextForExprOperand0() {
				return new IfNullFunctionThen<T, ET>() {

					@Override
					protected T nextForIfNullFunctionThen() {
						return IfNullFunctionArgument.this.nextForIfNullFunctionArgument();
					}

				};
			}

		};
	}

	@Override
	protected IIfNullFunctionThen<T, ET> nextForSingleOperand() {
		return new IfNullFunctionThen<T, ET>() {

			@Override
			protected T nextForIfNullFunctionThen() {
				return IfNullFunctionArgument.this.nextForIfNullFunctionArgument();
			}

		};
	}
}