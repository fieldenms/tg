package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperand3<T, ET extends AbstractEntity<?>> //
		extends SingleOperand<IExprOperationOrEnd3<T, ET>, ET> //
		implements IExprOperand3<T, ET> {

	protected abstract T nextForExprOperand3();

	@Override
	protected IExprOperationOrEnd3<T, ET> nextForAbstractSingleOperand() {
		return new ExprOperationOrEnd3<T, ET>() {

			@Override
			protected T nextForExprOperationOrEnd3() {
				return ExprOperand3.this.nextForExprOperand3();
			}

		};
	}
}