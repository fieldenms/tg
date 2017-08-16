package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

abstract class FunctionWhere0<T, ET extends AbstractEntity<?>> //
		extends Where<IFunctionComparisonOperator0<T, ET>, IFunctionCompoundCondition0<T, ET>, IFunctionWhere1<T, ET>, ET> //
		implements IFunctionWhere0<T, ET> {

	protected abstract T nextForFunctionWhere0();

	@Override
	protected IFunctionWhere1<T, ET> nextForAbstractWhere() {
		return new FunctionWhere1<T, ET>() {

			@Override
			protected T nextForFunctionWhere1() {
				return FunctionWhere0.this.nextForFunctionWhere0();
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition0<T, ET> nextForAbstractConditionalOperand() {
		return new FunctionCompoundCondition0<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition0() {
				return FunctionWhere0.this.nextForFunctionWhere0();
			}

		};
	}

	@Override
	protected IFunctionComparisonOperator0<T, ET> nextForAbstractSingleOperand() {
		return new FunctionComparisonOperator0<T, ET>() {

			@Override
			protected T nextForFunctionComparisonOperator0() {
				return FunctionWhere0.this.nextForFunctionWhere0();
			}
		};
	}
}