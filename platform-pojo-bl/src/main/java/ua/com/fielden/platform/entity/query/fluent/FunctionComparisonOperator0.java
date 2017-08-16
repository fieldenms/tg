package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;

abstract class FunctionComparisonOperator0<T, ET extends AbstractEntity<?>> //
		extends ComparisonOperator<IFunctionCompoundCondition0<T, ET>, ET> //
		implements IFunctionComparisonOperator0<T, ET> {

	protected abstract T nextForFunctionComparisonOperator0();

	@Override
	protected IFunctionCompoundCondition0<T, ET> nextForAbstractComparisonOperator() {
		return new FunctionCompoundCondition0<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition0() {
				return FunctionComparisonOperator0.this.nextForFunctionComparisonOperator0();
			}

		};
	}
}