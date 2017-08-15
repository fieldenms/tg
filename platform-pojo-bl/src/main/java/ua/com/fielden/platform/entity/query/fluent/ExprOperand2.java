package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

abstract class ExprOperand2<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IExprOperationOrEnd2<T, ET>, IExprOperand3<T, ET>, ET> implements IExprOperand2<T, ET> {

	abstract T nextForExprOperand2();

    @Override
    IExprOperand3<T, ET> nextForAbstractExprOperand() {
    	return new ExprOperand3<T, ET>(){

			@Override
			T nextForExprOperand3() {
				return ExprOperand2.this.nextForExprOperand2();
			}
        	
        };
    }

    @Override
    IExprOperationOrEnd2<T, ET> nextForAbstractSingleOperand() {
    	return new ExprOperationOrEnd2<T, ET>(){

			@Override
			T nextForExprOperationOrEnd2() {
				return ExprOperand2.this.nextForExprOperand2();
			}
        	
        };
    }
}