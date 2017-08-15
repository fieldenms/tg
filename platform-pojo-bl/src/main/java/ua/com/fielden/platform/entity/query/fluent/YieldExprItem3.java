package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd3;

abstract class YieldExprItem3<T, ET extends AbstractEntity<?>> extends AbstractYieldedItem<IYieldExprOperationOrEnd3<T, ET>, ET> implements IYieldExprItem3<T, ET> {

	abstract T nextForYieldExprItem3();

    @Override
    IYieldExprOperationOrEnd3<T, ET> nextForAbstractSingleOperand() {
    	return new YieldExprOperationOrEnd3<T, ET>(){

			@Override
			T nextForYieldExprOperationOrEnd3() {
				return YieldExprItem3.this.nextForYieldExprItem3();
			}
        	
        };
    }
}