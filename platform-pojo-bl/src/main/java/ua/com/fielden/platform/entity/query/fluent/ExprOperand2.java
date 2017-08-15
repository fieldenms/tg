package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand2;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand3;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperationOrEnd2;

abstract class ExprOperand2<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IExprOperationOrEnd2<T, ET>, IExprOperand3<T, ET>, ET> implements IExprOperand2<T, ET> {

	abstract T getParent3();

    @Override
    IExprOperand3<T, ET> getParent2() {
    	return new ExprOperand3<T, ET>(){

			@Override
			T getParent2() {
				return ExprOperand2.this.getParent3();
			}
        	
        };
    }

    @Override
    IExprOperationOrEnd2<T, ET> getParent() {
    	return new ExprOperationOrEnd2<T, ET>(){

			@Override
			T getParent3() {
				return ExprOperand2.this.getParent3();
			}
        	
        };
    }
}