package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;

abstract class FunctionComparisonOperator3<T, ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IFunctionCompoundCondition3<T, ET>, ET> //
		implements IFunctionComparisonOperator3<T, ET> {

	protected abstract T nextForFunctionComparisonOperator3();

	@Override
	protected IFunctionCompoundCondition3<T, ET> nextForAbstractComparisonOperator() {
		return new FunctionCompoundCondition3<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition3() {
				return FunctionComparisonOperator3.this.nextForFunctionComparisonOperator3();
			}

		};
	}
}