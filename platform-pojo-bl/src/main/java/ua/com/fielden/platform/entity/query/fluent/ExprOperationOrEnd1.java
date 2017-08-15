package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

abstract class ExprOperationOrEnd1<T, ET extends AbstractEntity<?>>
		extends AbstractExprOperationOrEnd<IExprOperand1<T, ET>, IExprOperationOrEnd0<T, ET>, ET>
		implements IExprOperationOrEnd1<T, ET> {

	abstract T nextForExprOperationOrEnd1();


	@Override
	IExprOperationOrEnd0<T, ET> nextForAbstractExprOperationOrEnd() {
		return new ExprOperationOrEnd0<T, ET>() {

			@Override
			T nextForExprOperationOrEnd0() {
				return ExprOperationOrEnd1.this.nextForExprOperationOrEnd1();
			}

		};
	}

	@Override
	IExprOperand1<T, ET> nextForAbstractArithmeticalOperator() {
		return new ExprOperand1<T, ET>() {

			@Override
			T nextForExprOperand1() {
				return ExprOperationOrEnd1.this.nextForExprOperationOrEnd1();
			}
		};
	}
}