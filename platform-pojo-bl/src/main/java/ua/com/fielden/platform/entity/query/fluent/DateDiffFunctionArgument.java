package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class DateDiffFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET> implements IDateDiffFunctionArgument<T, ET> {

	abstract T getParent3();

    @Override
    IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET> getParent2() {
    	return new ExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>(){

			@Override
			IDateDiffFunctionBetween<T, ET> getParent3() {
				return new DateDiffFunctionBetween<T, ET>(){

					@Override
					T getParent() {
						return DateDiffFunctionArgument.this.getParent3();
					}
					
				};
			}
        	
        };
    }

    @Override
    IDateDiffFunctionBetween<T, ET> getParent() {
    	return new DateDiffFunctionBetween<T, ET>(){

			@Override
			T getParent() {
				return DateDiffFunctionArgument.this.getParent3();
			}
        	
        };
    }
}