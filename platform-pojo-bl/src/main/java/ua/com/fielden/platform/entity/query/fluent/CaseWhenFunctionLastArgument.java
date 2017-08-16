package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionLastArgument<T, ET extends AbstractEntity<?>> //
		extends AbstractExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET> //
		implements ICaseWhenFunctionLastArgument<T, ET> {

	protected abstract T nextForCaseWhenFunctionLastArgument();

	@Override
	protected IExprOperand0<ICaseWhenFunctionEnd<T>, ET> nextForAbstractExprOperand() {
		return new ExprOperand0<ICaseWhenFunctionEnd<T>, ET>() {
			@Override
			protected ICaseWhenFunctionEnd<T> nextForExprOperand0() {
				return new CaseWhenFunctionEnd<T>() {

					@Override
					protected T nextForCaseWhenFunctionEnd() {
						return CaseWhenFunctionLastArgument.this.nextForCaseWhenFunctionLastArgument();
					}

				};
			}

		};
	}

	@Override
	protected ICaseWhenFunctionWhen<T, ET> nextForAbstractSingleOperand() {
		return new CaseWhenFunctionWhen<T, ET>() {

			@Override
			protected T nextForCaseWhenFunctionEnd() {
				return CaseWhenFunctionLastArgument.this.nextForCaseWhenFunctionLastArgument();
			}

		};
	}
}