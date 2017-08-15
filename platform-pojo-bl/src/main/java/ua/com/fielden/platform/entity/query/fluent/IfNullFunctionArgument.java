package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IIfNullFunctionThen;

abstract class IfNullFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IIfNullFunctionThen<T, ET>, IExprOperand0<IIfNullFunctionThen<T, ET>, ET>, ET> implements IIfNullFunctionArgument<T, ET> {

	abstract T nextForIfNullFunctionArgument();

    @Override
    IExprOperand0<IIfNullFunctionThen<T, ET>, ET> nextForAbstractExprOperand() {
    	return new ExprOperand0<IIfNullFunctionThen<T, ET>, ET>(){

			@Override
			IIfNullFunctionThen<T, ET> nextForExprOperand0() {
				return new IfNullFunctionThen<T, ET>(){

					@Override
					T nextForIfNullFunctionThen() {
						return IfNullFunctionArgument.this.nextForIfNullFunctionArgument();
					}
					
				};
			}
        	
        };
    }

    @Override
    IIfNullFunctionThen<T, ET> nextForAbstractSingleOperand() {
    	return new IfNullFunctionThen<T, ET>(){

			@Override
			T nextForIfNullFunctionThen() {
				return IfNullFunctionArgument.this.nextForIfNullFunctionArgument();
			}
        	
        };
    }
}