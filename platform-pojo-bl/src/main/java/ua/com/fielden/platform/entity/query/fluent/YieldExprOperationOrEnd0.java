package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprOperationOrEnd0<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IYieldExprItem0<T, ET>, T, ET> //
		implements IYieldExprOperationOrEnd0<T, ET> {

	protected abstract T nextForYieldExprOperationOrEnd0();

	@Override
	protected T nextForExprOperationOrEnd() {
		return nextForYieldExprOperationOrEnd0();
	}

	@Override
	protected IYieldExprItem0<T, ET> nextForArithmeticalOperator() {
		return new YieldExprItem0<T, ET>() {

			@Override
			protected T nextForYieldExprItem0() {
				return YieldExprOperationOrEnd0.this.nextForYieldExprOperationOrEnd0();
			}

		};
	}
}