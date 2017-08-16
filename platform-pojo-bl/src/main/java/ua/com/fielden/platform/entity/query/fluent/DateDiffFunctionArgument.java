package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class DateDiffFunctionArgument<T, ET extends AbstractEntity<?>> //
extends ExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET> //
implements IDateDiffFunctionArgument<T, ET> {

	protected abstract T nextForDateDiffFunctionArgument();

    @Override
    protected IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET> nextForAbstractExprOperand() {
    	return new ExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>(){

			@Override
			protected IDateDiffFunctionBetween<T, ET> nextForExprOperand0() {
				return new DateDiffFunctionBetween<T, ET>(){

					@Override
					protected T nextForDateDiffFunctionBetween() {
						return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument();
					}
					
				};
			}
        	
        };
    }

    @Override
    protected IDateDiffFunctionBetween<T, ET> nextForAbstractSingleOperand() {
    	return new DateDiffFunctionBetween<T, ET>(){

			@Override
			protected T nextForDateDiffFunctionBetween() {
				return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument();
			}
        	
        };
    }
}