package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionComparisonOperator3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionWhere3<T, ET extends AbstractEntity<?>> extends AbstractConditionalOperand<IFunctionComparisonOperator3<T, ET>, IFunctionCompoundCondition3<T, ET>, ET> implements IFunctionWhere3<T, ET> {

	abstract T getParent3();

    @Override
    IFunctionCompoundCondition3<T, ET> getParent2() {
    	return new FunctionCompoundCondition3<T, ET>(){

			@Override
			T getParent3() {
				return FunctionWhere3.this.getParent3();
			}
        	
        };
    }

    @Override
    IFunctionComparisonOperator3<T, ET> getParent() {
    	return new FunctionComparisonOperator3<T, ET>(){

			@Override
			T getParent5() {
				return FunctionWhere3.this.getParent3();
			}
        	
        };
    }
}