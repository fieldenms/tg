package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IExprOperand0<T, ET>, T, ET> //
		implements IExprOperationOrEnd0<T, ET> {

	protected abstract T nextForExprOperationOrEnd0();

	@Override
	protected IExprOperand0<T, ET> nextForAbstractArithmeticalOperator() {
		return new ExprOperand0<T, ET>() {

			@Override
			protected T nextForExprOperand0() {
				return ExprOperationOrEnd0.this.nextForExprOperationOrEnd0();
			}

		};
	}

	@Override
	protected T nextForAbstractExprOperationOrEnd() {
		return nextForExprOperationOrEnd0();
	}
}