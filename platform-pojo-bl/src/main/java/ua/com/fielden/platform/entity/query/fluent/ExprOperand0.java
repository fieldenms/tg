package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperand0<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IExprOperationOrEnd0<T, ET>, IExprOperand1<T, ET>, ET> //
		implements IExprOperand0<T, ET> {

	protected abstract T nextForExprOperand0();

	@Override
	protected IExprOperationOrEnd0<T, ET> nextForAbstractSingleOperand() {
		return new ExprOperationOrEnd0<T, ET>() {

			@Override
			protected T nextForExprOperationOrEnd0() {
				return ExprOperand0.this.nextForExprOperand0();
			}

		};
	}

	@Override
	protected IExprOperand1<T, ET> nextForAbstractExprOperand() {
		return new ExprOperand1<T, ET>() {

			@Override
			protected T nextForExprOperand1() {
				return ExprOperand0.this.nextForExprOperand0();
			}

		};
	}
}