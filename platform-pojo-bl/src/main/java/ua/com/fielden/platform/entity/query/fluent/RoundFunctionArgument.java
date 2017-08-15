package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

abstract class RoundFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IRoundFunctionTo<T>, IExprOperand0<IRoundFunctionTo<T>, ET>, ET> implements IRoundFunctionArgument<T, ET> {

	abstract T getParent3();

    @Override
    IExprOperand0<IRoundFunctionTo<T>, ET> getParent2() {
    	return new ExprOperand0<IRoundFunctionTo<T>, ET>(){

			@Override
			IRoundFunctionTo<T> getParent3() {
				return new RoundFunctionTo<T>(){

					@Override
					T getParent() {
						return RoundFunctionArgument.this.getParent3();
					}
					
				};
			}
        	
        };
    }

    @Override
    IRoundFunctionTo<T> getParent() {
    	return new RoundFunctionTo<T>(){

			@Override
			T getParent() {
				return RoundFunctionArgument.this.getParent3();
			}
        	
        };
    }
}