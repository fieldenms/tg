package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionWhere3<T, ET extends AbstractEntity<?>> //
		extends AbstractConditionalOperand<IFunctionComparisonOperator3<T, ET>, IFunctionCompoundCondition3<T, ET>, ET> //
		implements IFunctionWhere3<T, ET> {

	protected abstract T nextForFunctionWhere3();

	@Override
	protected IFunctionCompoundCondition3<T, ET> nextForAbstractConditionalOperand() {
		return new FunctionCompoundCondition3<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition3() {
				return FunctionWhere3.this.nextForFunctionWhere3();
			}

		};
	}

	@Override
	protected IFunctionComparisonOperator3<T, ET> nextForAbstractSingleOperand() {
		return new FunctionComparisonOperator3<T, ET>() {

			@Override
			protected T nextForFunctionComparisonOperator3() {
				return FunctionWhere3.this.nextForFunctionWhere3();
			}

		};
	}
}