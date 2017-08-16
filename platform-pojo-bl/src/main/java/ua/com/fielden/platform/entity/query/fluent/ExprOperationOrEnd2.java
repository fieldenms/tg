package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

abstract class ExprOperationOrEnd2<T, ET extends AbstractEntity<?>> //
		extends AbstractExprOperationOrEnd<IExprOperand2<T, ET>, IExprOperationOrEnd1<T, ET>, ET> //
		implements IExprOperationOrEnd2<T, ET> {

	protected abstract T nextForExprOperationOrEnd2();

	@Override
	protected IExprOperationOrEnd1<T, ET> nextForAbstractExprOperationOrEnd() {
		return new ExprOperationOrEnd1<T, ET>() {

			@Override
			protected T nextForExprOperationOrEnd1() {
				return ExprOperationOrEnd2.this.nextForExprOperationOrEnd2();
			}

		};
	}

	@Override
	protected IExprOperand2<T, ET> nextForAbstractArithmeticalOperator() {
		return new ExprOperand2<T, ET>() {

			@Override
			protected T nextForExprOperand2() {
				return ExprOperationOrEnd2.this.nextForExprOperationOrEnd2();
			}

		};
	}
}