package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionArgument<T, ET extends AbstractEntity<?>>
		extends AbstractExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET>
		implements ICaseWhenFunctionArgument<T, ET> {

	abstract T nextForCaseWhenFunctionArgument();

	@Override
	IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET> nextForAbstractExprOperand() {
		return new ExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>() {

			@Override
			ICaseWhenFunctionWhen<T, ET> nextForExprOperand0() {
				return new CaseWhenFunctionWhen<T, ET>() {

					@Override
					T nextForCaseWhenFunctionEnd() {
						return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument();
					}

				};
			}

		};
	}

	@Override
	ICaseWhenFunctionWhen<T, ET> nextForAbstractSingleOperand() {
		return new CaseWhenFunctionWhen<T, ET>() {

			@Override
			T nextForCaseWhenFunctionEnd() {
				return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument();
			}

		};
	}
}