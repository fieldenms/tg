package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class ConcatFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IConcatFunctionWith<T, ET>, IExprOperand0<IConcatFunctionWith<T, ET>, ET>, ET> //
		implements IConcatFunctionArgument<T, ET> {

	protected abstract T nextForConcatFunctionArgument();

	@Override
	protected IExprOperand0<IConcatFunctionWith<T, ET>, ET> nextForAbstractExprOperand() {
		return new ExprOperand0<IConcatFunctionWith<T, ET>, ET>() {

			@Override
			protected IConcatFunctionWith<T, ET> nextForExprOperand0() {
				return new ConcatFunctionWith<T, ET>() {

					@Override
					protected T nextForConcatFunctionWith() {
						return ConcatFunctionArgument.this.nextForConcatFunctionArgument();
					}

				};
			}

		};
	}

	@Override
	protected IConcatFunctionWith<T, ET> nextForAbstractSingleOperand() {
		return new ConcatFunctionWith<T, ET>() {

			@Override
			protected T nextForConcatFunctionWith() {
				return ConcatFunctionArgument.this.nextForConcatFunctionArgument();
			}

		};
	}
}