package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperand3<T, ET extends AbstractEntity<?>> extends AbstractSingleOperand<IExprOperationOrEnd3<T, ET>, ET> implements IExprOperand3<T, ET> {

	abstract T nextForExprOperand3();

    @Override
    IExprOperationOrEnd3<T, ET> nextForAbstractSingleOperand() {
    	return new ExprOperationOrEnd3<T, ET>(){

			@Override
			T nextForExprOperationOrEnd3() {
				return ExprOperand3.this.nextForExprOperand3();
			}
        	
        };
    }
}