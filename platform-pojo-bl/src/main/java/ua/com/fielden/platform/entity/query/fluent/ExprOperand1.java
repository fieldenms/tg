package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand1;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd1;

abstract class ExprOperand1<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IExprOperationOrEnd1<T, ET>, IExprOperand2<T, ET>, ET> implements IExprOperand1<T, ET> {

	abstract T getParent3();

    @Override
    IExprOperand2<T, ET> getParent2() {
    	return new ExprOperand2<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperand1.this.getParent3();
			}
        	
        };
    }

    @Override
    IExprOperationOrEnd1<T, ET> getParent() {
    	return new ExprOperationOrEnd1<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperand1.this.getParent3();
			}
        	
        };
    }
}