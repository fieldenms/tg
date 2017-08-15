package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd0;

abstract class YieldExprItem0<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd0<T, ET>, IYieldExprItem1<T, ET>, ET> implements IYieldExprItem0<T, ET> {
	
	abstract T nextForYieldExprItem0();

    @Override
    IYieldExprItem1<T, ET> nextForAbstractYieldExprOperand() {
    	return new YieldExprItem1<T, ET>(){

			@Override
			T nextForYieldExprItem1() {
				return YieldExprItem0.this.nextForYieldExprItem0();
			}
        	
        };
    }

    @Override
    IYieldExprOperationOrEnd0<T, ET> nextForAbstractSingleOperand() {
        return new YieldExprOperationOrEnd0<T, ET>(){
			@Override
			T nextForYieldExprOperationOrEnd0() {
				return YieldExprItem0.this.nextForYieldExprItem0();
			}
        	
        };
    }
}