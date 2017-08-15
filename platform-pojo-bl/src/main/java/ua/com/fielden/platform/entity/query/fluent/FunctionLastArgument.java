package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IFunctionLastArgument;

abstract class FunctionLastArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<T, IExprOperand0<T, ET>, ET> implements IFunctionLastArgument<T, ET> {

	abstract T nextForFunctionLastArgument();

    @Override
    IExprOperand0<T, ET> nextForAbstractExprOperand() {
    	return new ExprOperand0<T, ET>(){

			@Override
			T nextForExprOperand0() {
				return FunctionLastArgument.this.nextForFunctionLastArgument();
			}
        	
        };
    }

    @Override
    T nextForAbstractSingleOperand() {
        return nextForFunctionLastArgument();
    }
}