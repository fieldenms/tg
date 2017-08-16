package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;

abstract class FunctionComparisonOperator2<T, ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IFunctionCompoundCondition2<T, ET>, ET> //
		implements IFunctionComparisonOperator2<T, ET> {

	protected abstract T nextForFunctionComparisonOperator2();

	@Override
	protected IFunctionCompoundCondition2<T, ET> nextForAbstractComparisonOperator() {
		return new FunctionCompoundCondition2<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition2() {
				return FunctionComparisonOperator2.this.nextForFunctionComparisonOperator2();
			}

		};
	}
}