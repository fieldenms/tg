package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;

abstract class FunctionComparisonOperator1<T, ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IFunctionCompoundCondition1<T, ET>, ET> implements IFunctionComparisonOperator1<T, ET> {

	abstract T getParent5();

    @Override
    IFunctionCompoundCondition1<T, ET> getParent1() {
    	return new FunctionCompoundCondition1<T, ET>(){

			@Override
			T getParent3() {
				return FunctionComparisonOperator1.this.getParent5();
			}
        	
        };
    }
}