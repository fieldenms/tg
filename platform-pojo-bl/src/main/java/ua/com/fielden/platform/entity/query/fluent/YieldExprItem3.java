package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprItem3<T, ET extends AbstractEntity<?>> extends AbstractYieldedItem<IYieldExprOperationOrEnd3<T, ET>, ET> implements IYieldExprItem3<T, ET> {

	abstract T getParent2();

    @Override
    IYieldExprOperationOrEnd3<T, ET> getParent() {
    	return new YieldExprOperationOrEnd3<T, ET>(){

			@Override
			T getParent3() {
				return YieldExprItem3.this.getParent2();
			}
        	
        };
    }
}