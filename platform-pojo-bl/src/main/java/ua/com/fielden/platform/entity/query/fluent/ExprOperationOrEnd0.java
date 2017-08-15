package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperationOrEnd0<T, ET extends AbstractEntity<?>> extends AbstractExprOperationOrEnd<IExprOperand0<T, ET>, T, ET> implements IExprOperationOrEnd0<T, ET> {

	abstract T getParent3();

    @Override
    IExprOperand0<T, ET> getParent() {
    	return new ExprOperand0<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperationOrEnd0.this.getParent3();
			}
        	
        };
    }

    @Override
    T getParent2() {
    	return getParent3();
    }
}