package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;

abstract class YieldExprOperationOrEnd2<T, ET extends AbstractEntity<?>> //
		extends AbstractExprOperationOrEnd<IYieldExprItem2<T, ET>, IYieldExprOperationOrEnd1<T, ET>, ET> //
		implements IYieldExprOperationOrEnd2<T, ET> {

	protected abstract T nextForYieldExprOperationOrEnd2();

	@Override
	protected IYieldExprOperationOrEnd1<T, ET> nextForAbstractExprOperationOrEnd() {
		return new YieldExprOperationOrEnd1<T, ET>() {

			@Override
			protected T nextForYieldExprOperationOrEnd1() {
				return YieldExprOperationOrEnd2.this.nextForYieldExprOperationOrEnd2();
			}

		};
	}

	@Override
	protected IYieldExprItem2<T, ET> nextForAbstractArithmeticalOperator() {

		return new YieldExprItem2<T, ET>() {

			@Override
			protected T nextForYieldExprItem2() {
				return YieldExprOperationOrEnd2.this.nextForYieldExprOperationOrEnd2();
			}

		};
	}
}