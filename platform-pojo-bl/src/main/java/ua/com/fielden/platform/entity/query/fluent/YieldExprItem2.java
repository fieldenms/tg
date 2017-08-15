package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprItem3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IYieldExprOperationOrEnd2;

abstract class YieldExprItem2<T, ET extends AbstractEntity<?>> extends AbstractYieldExprOperand<IYieldExprOperationOrEnd2<T, ET>, IYieldExprItem3<T, ET>, ET> implements IYieldExprItem2<T, ET> {

	abstract T nextForYieldExprItem2();

    @Override
    IYieldExprItem3<T, ET> nextForAbstractYieldExprOperand() {
    	return new YieldExprItem3<T, ET>(){

			@Override
			T nextForYieldExprItem3() {
				return YieldExprItem2.this.nextForYieldExprItem2();
			}
        	
        };
    }

    @Override
    IYieldExprOperationOrEnd2<T, ET> nextForAbstractSingleOperand() {
    	return new YieldExprOperationOrEnd2<T, ET>(){

			@Override
			T nextForYieldExprOperationOrEnd2() {
				return YieldExprItem2.this.nextForYieldExprItem2();
			}
        	
        };
    }
}