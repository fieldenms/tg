package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionCompoundCondition3<T, ET extends AbstractEntity<?>> //
		extends CompoundCondition<IFunctionWhere3<T, ET>, IFunctionCompoundCondition2<T, ET>> //
		implements IFunctionCompoundCondition3<T, ET> {

	protected abstract T nextForFunctionCompoundCondition3();

	@Override
	protected IFunctionWhere3<T, ET> nextForLogicalCondition() {
		return new FunctionWhere3<T, ET>() {

			@Override
			protected T nextForFunctionWhere3() {
				return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3();
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition2<T, ET> nextForCompoundCondition() {
		return new FunctionCompoundCondition2<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition2() {
				return FunctionCompoundCondition3.this.nextForFunctionCompoundCondition3();
			}

		};
	}
}