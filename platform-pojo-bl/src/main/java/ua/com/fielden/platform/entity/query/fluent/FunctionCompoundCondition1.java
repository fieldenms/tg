package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere1;

abstract class FunctionCompoundCondition1<T, ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IFunctionWhere1<T, ET>, IFunctionCompoundCondition0<T, ET>> implements IFunctionCompoundCondition1<T, ET> {

	abstract T getParent3();

    @Override
    IFunctionWhere1<T, ET> getParent() {
    	return new FunctionWhere1<T, ET>(){

			@Override
			T getParent4() {
				return FunctionCompoundCondition1.this.getParent3();
			}
        	
        };
    }

    @Override
    IFunctionCompoundCondition0<T, ET> getParent2() {
    	return new FunctionCompoundCondition0<T, ET>(){

			@Override
			T getParent() {
				return FunctionCompoundCondition1.this.getParent3();
			}
        	
        };
    }
}