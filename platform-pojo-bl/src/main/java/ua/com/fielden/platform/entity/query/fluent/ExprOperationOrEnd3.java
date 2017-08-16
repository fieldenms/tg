package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperationOrEnd3<T, ET extends AbstractEntity<?>> //
		extends AbstractExprOperationOrEnd<IExprOperand3<T, ET>, IExprOperationOrEnd2<T, ET>, ET> //
		implements IExprOperationOrEnd3<T, ET> {

	protected abstract T nextForExprOperationOrEnd3();

	@Override
	protected IExprOperationOrEnd2<T, ET> nextForAbstractExprOperationOrEnd() {
		return new ExprOperationOrEnd2<T, ET>() {

			@Override
			protected T nextForExprOperationOrEnd2() {
				return ExprOperationOrEnd3.this.nextForExprOperationOrEnd3();
			}

		};
	}

	@Override
	protected IExprOperand3<T, ET> nextForAbstractArithmeticalOperator() {
		return new ExprOperand3<T, ET>() {

			@Override
			protected T nextForExprOperand3() {
				return ExprOperationOrEnd3.this.nextForExprOperationOrEnd3();
			}
		};
	}
}