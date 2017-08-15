package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionCompoundCondition2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionWhere2;

abstract class FunctionCompoundCondition2<T, ET extends AbstractEntity<?>> extends AbstractCompoundCondition<IFunctionWhere2<T, ET>, IFunctionCompoundCondition1<T, ET>> implements IFunctionCompoundCondition2<T, ET> {

	abstract T getParent3();

    @Override
    IFunctionWhere2<T, ET> getParent() {
    	return new FunctionWhere2<T, ET>() {

			@Override
			T getParent4() {
				return FunctionCompoundCondition2.this.getParent3();
			}
        	
        };
    }

    @Override
    IFunctionCompoundCondition1<T, ET> getParent2() {
    	return new FunctionCompoundCondition1<T, ET>(){

			@Override
			T getParent3() {
				return FunctionCompoundCondition2.this.getParent3();
			}
        	
        };
    }
}