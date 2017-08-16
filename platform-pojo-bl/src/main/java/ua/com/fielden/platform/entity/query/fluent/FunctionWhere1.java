package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

abstract class FunctionWhere1<T, ET extends AbstractEntity<?>> //
		extends Where<IFunctionComparisonOperator1<T, ET>, IFunctionCompoundCondition1<T, ET>, IFunctionWhere2<T, ET>, ET> //
		implements IFunctionWhere1<T, ET> {

	protected abstract T nextForFunctionWhere1();

	@Override
	protected IFunctionWhere2<T, ET> nextForAbstractWhere() {
		return new FunctionWhere2<T, ET>() {

			@Override
			protected T nextForFunctionWhere2() {
				return FunctionWhere1.this.nextForFunctionWhere1();
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition1<T, ET> nextForAbstractConditionalOperand() {
		return new FunctionCompoundCondition1<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition1() {
				return FunctionWhere1.this.nextForFunctionWhere1();
			}

		};
	}

	@Override
	protected IFunctionComparisonOperator1<T, ET> nextForAbstractSingleOperand() {
		return new FunctionComparisonOperator1<T, ET>() {

			@Override
			protected T nextForFunctionComparisonOperator1() {
				return FunctionWhere1.this.nextForFunctionWhere1();
			}

		};
	}
}