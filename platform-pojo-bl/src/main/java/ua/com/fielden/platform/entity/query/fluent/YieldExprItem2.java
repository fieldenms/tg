package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;

abstract class YieldExprItem2<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd2<T, ET>, IYieldExprItem3<T, ET>, ET> implements IYieldExprItem2<T, ET> {

	abstract T getParent3();

    @Override
    IYieldExprItem3<T, ET> getParent2() {
    	return new YieldExprItem3<T, ET>(){

			@Override
			T getParent2() {
				return YieldExprItem2.this.getParent3();
			}
        	
        };
    }

    @Override
    IYieldExprOperationOrEnd2<T, ET> getParent() {
    	return new YieldExprOperationOrEnd2<T, ET>(){

			@Override
			T getParent3() {
				return YieldExprItem2.this.getParent3();
			}
        	
        };
    }
}