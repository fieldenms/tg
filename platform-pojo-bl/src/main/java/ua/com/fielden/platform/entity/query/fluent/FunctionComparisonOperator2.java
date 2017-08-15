package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;

abstract class FunctionComparisonOperator2<T, ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IFunctionCompoundCondition2<T, ET>, ET> implements IFunctionComparisonOperator2<T, ET> {

	abstract T getParent5();

    @Override
    IFunctionCompoundCondition2<T, ET> getParent1() {
        return new FunctionCompoundCondition2<T, ET>(){

			@Override
			T getParent3() {
				return FunctionComparisonOperator2.this.getParent5();
			}
        	
        };
    }
}