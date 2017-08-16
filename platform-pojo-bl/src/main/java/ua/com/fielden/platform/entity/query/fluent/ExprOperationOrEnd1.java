package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

abstract class ExprOperationOrEnd1<T, ET extends AbstractEntity<?>> //
		extends AbstractExprOperationOrEnd<IExprOperand1<T, ET>, IExprOperationOrEnd0<T, ET>, ET> //
		implements IExprOperationOrEnd1<T, ET> {

	protected abstract T nextForExprOperationOrEnd1();

	@Override
	protected IExprOperationOrEnd0<T, ET> nextForAbstractExprOperationOrEnd() {
		return new ExprOperationOrEnd0<T, ET>() {

			@Override
			protected T nextForExprOperationOrEnd0() {
				return ExprOperationOrEnd1.this.nextForExprOperationOrEnd1();
			}

		};
	}

	@Override
	protected IExprOperand1<T, ET> nextForAbstractArithmeticalOperator() {
		return new ExprOperand1<T, ET>() {

			@Override
			protected T nextForExprOperand1() {
				return ExprOperationOrEnd1.this.nextForExprOperationOrEnd1();
			}
		};
	}
}