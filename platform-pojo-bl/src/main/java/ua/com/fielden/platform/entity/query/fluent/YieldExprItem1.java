package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd1;

abstract class YieldExprItem1<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd1<T, ET>, IYieldExprItem2<T, ET>, ET> implements IYieldExprItem1<T, ET> {

	abstract T getParent3();

    @Override
    IYieldExprItem2<T, ET> getParent2() {
    	return new YieldExprItem2<T, ET>(){

			@Override
			T getParent3() {
				return YieldExprItem1.this.getParent3();
			}
        	
        };
    }

    @Override
    IYieldExprOperationOrEnd1<T, ET> getParent() {
    	return new YieldExprOperationOrEnd1<T, ET>(){

			@Override
			T getParent3() {
				return YieldExprItem1.this.getParent3();
			}
        	
        };
    }
}