package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class FunctionLastArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<T, IExprOperand0<T, ET>, ET> implements IFunctionLastArgument<T, ET> {

	abstract T getParent3();

    @Override
    IExprOperand0<T, ET> getParent2() {
    	return new ExprOperand0<T, ET>(){

			@Override
			T getParent3() {
				return FunctionLastArgument.this.getParent3();
			}
        	
        };
    }

    @Override
    T getParent() {
        return getParent3();
    }
}