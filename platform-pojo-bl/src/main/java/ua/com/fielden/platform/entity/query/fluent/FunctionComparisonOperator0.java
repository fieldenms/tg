package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;

abstract class FunctionComparisonOperator0<T, ET extends AbstractEntity<?>> extends AbstractComparisonOperator<IFunctionCompoundCondition0<T, ET>, ET> implements IFunctionComparisonOperator0<T, ET> {

	abstract T nextForFunctionComparisonOperator0();

    @Override
    IFunctionCompoundCondition0<T, ET> nextForAbstractComparisonOperator() {
    	return new FunctionCompoundCondition0<T, ET>(){

			@Override
			T nextForFunctionCompoundCondition0() {
				return FunctionComparisonOperator0.this.nextForFunctionComparisonOperator0();
			}
        	
        };
    }
}