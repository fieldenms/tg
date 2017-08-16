package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprOperationOrEnd3<T, ET extends AbstractEntity<?>> //
		extends ExprOperationOrEnd<IYieldExprItem3<T, ET>, IYieldExprOperationOrEnd2<T, ET>, ET> //
		implements IYieldExprOperationOrEnd3<T, ET> {

	protected abstract T nextForYieldExprOperationOrEnd3();

	@Override
	protected IYieldExprOperationOrEnd2<T, ET> nextForExprOperationOrEnd() {
		return new YieldExprOperationOrEnd2<T, ET>() {

			@Override
			protected T nextForYieldExprOperationOrEnd2() {
				return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3();
			}

		};
	}

	@Override
	protected IYieldExprItem3<T, ET> nextForArithmeticalOperator() {
		return new YieldExprItem3<T, ET>() {

			@Override
			protected T nextForYieldExprItem3() {
				return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3();
			}

		};
	}
}