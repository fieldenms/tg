package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

abstract class FunctionWhere1<T, ET extends AbstractEntity<?>> extends
		AbstractWhere<IFunctionComparisonOperator1<T, ET>, IFunctionCompoundCondition1<T, ET>, IFunctionWhere2<T, ET>, ET>
		implements IFunctionWhere1<T, ET> {

	abstract T getParent4();

	@Override
	protected IFunctionWhere2<T, ET> getParent3() {
		return new FunctionWhere2<T, ET>() {

			@Override
			T getParent4() {
				return FunctionWhere1.this.getParent4();
			}

		};
	}

	@Override
	IFunctionCompoundCondition1<T, ET> getParent2() {
		return new FunctionCompoundCondition1<T, ET>() {

			@Override
			T getParent3() {
				return FunctionWhere1.this.getParent4();
			}

		};
	}

	@Override
	IFunctionComparisonOperator1<T, ET> getParent() {
		return new FunctionComparisonOperator1<T, ET>() {

			@Override
			T getParent5() {
				return FunctionWhere1.this.getParent4();
			}

		};
	}
}