package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere3;

abstract class FunctionCompoundCondition3<T, ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IFunctionWhere3<T, ET>, IFunctionCompoundCondition2<T, ET>> implements IFunctionCompoundCondition3<T, ET> {

	abstract T getParent3();

    @Override
    IFunctionWhere3<T, ET> getParent() {
    	return new FunctionWhere3<T, ET>(){

			@Override
			T getParent3() {
				return FunctionCompoundCondition3.this.getParent3();
			}
        	
        };
    }

    @Override
    IFunctionCompoundCondition2<T, ET> getParent2() {
    	return new FunctionCompoundCondition2<T, ET>(){

			@Override
			T getParent3() {
				return FunctionCompoundCondition3.this.getParent3();
			}
        	
        };
    }
}