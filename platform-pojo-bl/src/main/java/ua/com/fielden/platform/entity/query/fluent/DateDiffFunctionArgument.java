package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class DateDiffFunctionArgument<T, ET extends AbstractEntity<?>> extends AbstractExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET> implements IDateDiffFunctionArgument<T, ET> {

	abstract T nextForDateDiffFunctionArgument();

    @Override
    IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET> nextForAbstractExprOperand() {
    	return new ExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>(){

			@Override
			IDateDiffFunctionBetween<T, ET> nextForExprOperand0() {
				return new DateDiffFunctionBetween<T, ET>(){

					@Override
					T nextForDateDiffFunctionBetween() {
						return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument();
					}
					
				};
			}
        	
        };
    }

    @Override
    IDateDiffFunctionBetween<T, ET> nextForAbstractSingleOperand() {
    	return new DateDiffFunctionBetween<T, ET>(){

			@Override
			T nextForDateDiffFunctionBetween() {
				return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument();
			}
        	
        };
    }
}