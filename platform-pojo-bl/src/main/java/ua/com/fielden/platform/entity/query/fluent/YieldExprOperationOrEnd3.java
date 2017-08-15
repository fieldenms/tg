package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprOperationOrEnd3<T, ET extends AbstractEntity<?>>
		extends AbstractExprOperationOrEnd<IYieldExprItem3<T, ET>, IYieldExprOperationOrEnd2<T, ET>, ET>
		implements IYieldExprOperationOrEnd3<T, ET> {

	abstract T nextForYieldExprOperationOrEnd3();

	@Override
	IYieldExprOperationOrEnd2<T, ET> nextForAbstractExprOperationOrEnd() {
		return new YieldExprOperationOrEnd2<T, ET>() {

			@Override
			T nextForYieldExprOperationOrEnd2() {
				return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3();
			}

		};
	}

	@Override
	IYieldExprItem3<T, ET> nextForAbstractArithmeticalOperator() {
		return new YieldExprItem3<T, ET>() {

			@Override
			T nextForYieldExprItem3() {
				return YieldExprOperationOrEnd3.this.nextForYieldExprOperationOrEnd3();
			}

		};
	}
}