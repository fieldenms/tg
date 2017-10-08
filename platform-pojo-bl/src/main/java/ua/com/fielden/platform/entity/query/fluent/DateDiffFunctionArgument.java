package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IDateDiffFunctionBetween;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class DateDiffFunctionArgument<T, ET extends AbstractEntity<?>> //
               extends ExprOperand<IDateDiffFunctionBetween<T, ET>, IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>, ET> //
               implements IDateDiffFunctionArgument<T, ET> {

    protected DateDiffFunctionArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForDateDiffFunctionArgument(final Tokens tokens);

    @Override
    protected IExprOperand0<IDateDiffFunctionBetween<T, ET>, ET> nextForExprOperand(final Tokens tokens) {
    	return new ExprOperand0<IDateDiffFunctionBetween<T, ET>, ET>(tokens) {

			@Override
			protected IDateDiffFunctionBetween<T, ET> nextForExprOperand0(final Tokens tokens) {
				return new DateDiffFunctionBetween<T, ET>(tokens) {

					@Override
					protected T nextForDateDiffFunctionBetween(final Tokens tokens) {
						return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument(tokens);
					}
					
				};
			}
        	
        };
    }

    @Override
    protected IDateDiffFunctionBetween<T, ET> nextForSingleOperand(final Tokens tokens) {
    	return new DateDiffFunctionBetween<T, ET>(tokens) {

			@Override
			protected T nextForDateDiffFunctionBetween(final Tokens tokens) {
				return DateDiffFunctionArgument.this.nextForDateDiffFunctionArgument(tokens);
			}
        	
        };
    }
}