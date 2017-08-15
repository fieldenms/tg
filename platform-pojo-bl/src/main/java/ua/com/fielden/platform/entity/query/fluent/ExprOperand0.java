package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd0;

abstract class ExprOperand0<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IExprOperationOrEnd0<T, ET>, IExprOperand1<T, ET>, ET> implements IExprOperand0<T, ET> {
	abstract T getParent3();

    @Override
    IExprOperationOrEnd0<T, ET> getParent() {
    	return new ExprOperationOrEnd0<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperand0.this.getParent3();
			}
        	
        };
    }

    @Override
    IExprOperand1<T, ET> getParent2() {
    	return new ExprOperand1<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperand0.this.getParent3();
			}
        	
        };
    }
}