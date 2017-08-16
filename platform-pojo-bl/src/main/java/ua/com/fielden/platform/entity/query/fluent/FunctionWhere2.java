package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionWhere2<T, ET extends AbstractEntity<?>> //
		extends AbstractWhere<IFunctionComparisonOperator2<T, ET>, IFunctionCompoundCondition2<T, ET>, IFunctionWhere3<T, ET>, ET> //
		implements IFunctionWhere2<T, ET> {

	protected abstract T nextForFunctionWhere2();

	@Override
	protected IFunctionWhere3<T, ET> nextForAbstractWhere() {
		return new FunctionWhere3<T, ET>() {

			@Override
			protected T nextForFunctionWhere3() {
				return FunctionWhere2.this.nextForFunctionWhere2();
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition2<T, ET> nextForAbstractConditionalOperand() {
		return new FunctionCompoundCondition2<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition2() {
				return FunctionWhere2.this.nextForFunctionWhere2();
			}

		};
	}

	@Override
	protected IFunctionComparisonOperator2<T, ET> nextForAbstractSingleOperand() {
		return new FunctionComparisonOperator2<T, ET>() {

			@Override
			protected T nextForFunctionComparisonOperator2() {
				return FunctionWhere2.this.nextForFunctionWhere2();
			}

		};
	}
}