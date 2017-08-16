package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

abstract class FunctionCompoundCondition2<T, ET extends AbstractEntity<?>> //
		extends AbstractCompoundCondition<IFunctionWhere2<T, ET>, IFunctionCompoundCondition1<T, ET>> //
		implements IFunctionCompoundCondition2<T, ET> {

	protected abstract T nextForFunctionCompoundCondition2();

	@Override
	protected IFunctionWhere2<T, ET> nextForAbstractLogicalCondition() {
		return new FunctionWhere2<T, ET>() {

			@Override
			protected T nextForFunctionWhere2() {
				return FunctionCompoundCondition2.this.nextForFunctionCompoundCondition2();
			}

		};
	}

	@Override
	protected IFunctionCompoundCondition1<T, ET> nextForAbstractCompoundCondition() {
		return new FunctionCompoundCondition1<T, ET>() {

			@Override
			protected T nextForFunctionCompoundCondition1() {
				return FunctionCompoundCondition2.this.nextForFunctionCompoundCondition2();
			}

		};
	}
}