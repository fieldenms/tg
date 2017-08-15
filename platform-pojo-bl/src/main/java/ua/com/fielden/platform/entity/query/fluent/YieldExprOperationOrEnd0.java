package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprOperationOrEnd0<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IYieldExprItem0<T, ET>, T, ET> implements IYieldExprOperationOrEnd0<T, ET> {

	abstract T nextForYieldExprOperationOrEnd0();

    @Override
    T nextForAbstractExprOperationOrEnd() {
        return nextForYieldExprOperationOrEnd0();
    }

    @Override
    IYieldExprItem0<T, ET> nextForAbstractArithmeticalOperator() {
    	return new YieldExprItem0<T, ET>(){

			@Override
			T nextForYieldExprItem0() {
				return YieldExprOperationOrEnd0.this.nextForYieldExprOperationOrEnd0();
			}
        	
        };
    }
}