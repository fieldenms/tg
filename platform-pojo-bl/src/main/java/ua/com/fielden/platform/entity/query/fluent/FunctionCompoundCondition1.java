package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

abstract class FunctionCompoundCondition1<T, ET extends AbstractEntity<?>> //
		extends AbstractCompoundCondition<IFunctionWhere1<T, ET>, IFunctionCompoundCondition0<T, ET>> //
		implements IFunctionCompoundCondition1<T, ET> {

	protected abstract T nextForFunctionCompoundCondition1();

	@Override
	protected IFunctionWhere1<T, ET> nextForAbstractLogicalCondition() {
		return new FunctionWhere1<T, ET>() {

			@Override
			protected T nextForFunctionWhere1() {
				return FunctionCompoundCondition1.this.nextForFunctionCompoundCondition1();
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition0<T, ET> nextForAbstractCompoundCondition() {
		return new FunctionCompoundCondition0<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition0() {
				return FunctionCompoundCondition1.this.nextForFunctionCompoundCondition1();
			}

		};
	}
}