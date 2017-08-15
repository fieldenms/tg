package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd3;

abstract class ExprOperand3<T, ET extends AbstractEntity<?>> extends AbstractSingleOperand<IExprOperationOrEnd3<T, ET>, ET> implements IExprOperand3<T, ET> {

	abstract T getParent2();

    @Override
    IExprOperationOrEnd3<T, ET> getParent() {
    	return new ExprOperationOrEnd3<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperand3.this.getParent2();
			}
        	
        };
    }
}