package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;

abstract class YieldExprItem1<T, ET extends AbstractEntity<?>> //
		extends AbstractYieldExprOperand<IYieldExprOperationOrEnd1<T, ET>, IYieldExprItem2<T, ET>, ET> //
		implements IYieldExprItem1<T, ET> {

	protected abstract T nextForYieldExprItem1();

	@Override
	protected IYieldExprItem2<T, ET> nextForAbstractYieldExprOperand() {
		return new YieldExprItem2<T, ET>() {

			@Override
			protected T nextForYieldExprItem2() {
				return YieldExprItem1.this.nextForYieldExprItem1();
			}

		};
	}

	@Override
	protected IYieldExprOperationOrEnd1<T, ET> nextForAbstractSingleOperand() {
		return new YieldExprOperationOrEnd1<T, ET>() {

			@Override
			protected T nextForYieldExprOperationOrEnd1() {
				return YieldExprItem1.this.nextForYieldExprItem1();
			}

		};
	}
}