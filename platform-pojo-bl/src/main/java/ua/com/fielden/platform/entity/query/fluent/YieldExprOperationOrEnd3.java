package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprOperationOrEnd3<T, ET extends AbstractEntity<?>>
		extends AbstractExprOperationOrEnd<IYieldExprItem3<T, ET>, IYieldExprOperationOrEnd2<T, ET>, ET>
		implements IYieldExprOperationOrEnd3<T, ET> {

	abstract T getParent3();

	@Override
	IYieldExprOperationOrEnd2<T, ET> getParent2() {
		return new YieldExprOperationOrEnd2<T, ET>() {

			@Override
			T getParent3() {
				return YieldExprOperationOrEnd3.this.getParent3();
			}

		};
	}

	@Override
	IYieldExprItem3<T, ET> getParent() {
		return new YieldExprItem3<T, ET>() {

			@Override
			T getParent2() {
				return YieldExprOperationOrEnd3.this.getParent3();
			}

		};
	}
}