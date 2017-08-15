package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;

abstract class YieldExprOperationOrEnd1<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IYieldExprItem1<T, ET>, IYieldExprOperationOrEnd0<T, ET>, ET> implements IYieldExprOperationOrEnd1<T, ET> {

	abstract T getParent3();

    @Override
    IYieldExprOperationOrEnd0<T, ET> getParent2() {
    	return new YieldExprOperationOrEnd0<T, ET>(){

			@Override
			T getParent3() {
				return YieldExprOperationOrEnd1.this.getParent3();
			}
        	
        };
    }

    @Override
    IYieldExprItem1<T, ET> getParent() {
    	return new YieldExprItem1<T, ET>(){

			@Override
			T getParent3() {
				return YieldExprOperationOrEnd1.this.getParent3();
			}
        	
        };
    }
}