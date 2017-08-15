package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;

abstract class FunctionComparisonOperator0<T, ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IFunctionCompoundCondition0<T, ET>, ET> implements IFunctionComparisonOperator0<T, ET> {

	abstract T getParent5();

    @Override
    IFunctionCompoundCondition0<T, ET> getParent1() {
    	return new FunctionCompoundCondition0<T, ET>(){

			@Override
			T getParent() {
				return FunctionComparisonOperator0.this.getParent5();
			}
        	
        };
    }
}