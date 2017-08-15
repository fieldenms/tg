package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprOperationOrEnd0<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IYieldExprItem0<T, ET>, T, ET> implements IYieldExprOperationOrEnd0<T, ET> {

	abstract T getParent3();

    @Override
    T getParent2() {
        return getParent3();
    }

    @Override
    IYieldExprItem0<T, ET> getParent() {
    	return new YieldExprItem0<T, ET>(){

			@Override
			T getParent3() {
				return YieldExprOperationOrEnd0.this.getParent3();
			}
        	
        };
    }
}