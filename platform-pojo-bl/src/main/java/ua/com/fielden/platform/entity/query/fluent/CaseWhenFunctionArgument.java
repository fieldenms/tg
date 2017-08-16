package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET> //
		implements ICaseWhenFunctionArgument<T, ET> {

	protected abstract T nextForCaseWhenFunctionArgument();

	@Override
	protected IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET> nextForExprOperand() {
		return new ExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>() {

			@Override
			protected ICaseWhenFunctionWhen<T, ET> nextForExprOperand0() {
				return new CaseWhenFunctionWhen<T, ET>() {

					@Override
					protected T nextForCaseWhenFunctionEnd() {
						return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument();
					}

				};
			}

		};
	}

	@Override
	protected ICaseWhenFunctionWhen<T, ET> nextForSingleOperand() {
		return new CaseWhenFunctionWhen<T, ET>() {

			@Override
			protected T nextForCaseWhenFunctionEnd() {
				return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument();
			}

		};
	}
}