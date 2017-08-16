package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprItem0<T, ET extends AbstractEntity<?>> //
		extends YieldExprOperand<IYieldExprOperationOrEnd0<T, ET>, IYieldExprItem1<T, ET>, ET> //
		implements IYieldExprItem0<T, ET> {

	protected abstract T nextForYieldExprItem0();

	@Override
	protected IYieldExprItem1<T, ET> nextForYieldExprOperand() {
		return new YieldExprItem1<T, ET>() {

			@Override
			protected T nextForYieldExprItem1() {
				return YieldExprItem0.this.nextForYieldExprItem0();
			}

		};
	}

	@Override
	protected IYieldExprOperationOrEnd0<T, ET> nextForSingleOperand() {
		return new YieldExprOperationOrEnd0<T, ET>() {
			@Override
			protected T nextForYieldExprOperationOrEnd0() {
				return YieldExprItem0.this.nextForYieldExprItem0();
			}

		};
	}
}