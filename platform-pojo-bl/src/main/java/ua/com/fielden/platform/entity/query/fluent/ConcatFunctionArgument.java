package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class ConcatFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IConcatFunctionWith<T, ET>, IExprOperand0<IConcatFunctionWith<T, ET>, ET>, ET> implements IConcatFunctionArgument<T, ET> {
	abstract T getParent3();

    @Override
    IExprOperand0<IConcatFunctionWith<T, ET>, ET> getParent2() {
    	return new ExprOperand0<IConcatFunctionWith<T, ET>, ET>(){

			@Override
			IConcatFunctionWith<T, ET> getParent3() {
				return new ConcatFunctionWith<T, ET>(){

					@Override
					T getParent() {
						return ConcatFunctionArgument.this.getParent3();
					}
					
				};
			}
        	
        };
    }

    @Override
    IConcatFunctionWith<T, ET> getParent() {
        return new ConcatFunctionWith<T, ET>(){

			@Override
			T getParent() {
				return ConcatFunctionArgument.this.getParent3();
			}
        	
        };
    }
}