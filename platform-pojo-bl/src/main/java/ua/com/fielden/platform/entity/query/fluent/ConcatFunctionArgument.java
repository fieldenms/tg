package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class ConcatFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IConcatFunctionWith<T, ET>, IExprOperand0<IConcatFunctionWith<T, ET>, ET>, ET> //
		implements IConcatFunctionArgument<T, ET> {

    protected ConcatFunctionArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForConcatFunctionArgument(final Tokens tokens);

	@Override
	protected IExprOperand0<IConcatFunctionWith<T, ET>, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<IConcatFunctionWith<T, ET>, ET>(tokens) {

			@Override
			protected IConcatFunctionWith<T, ET> nextForExprOperand0(final Tokens tokens) {
				return new ConcatFunctionWith<T, ET>(tokens) {

					@Override
					protected T nextForConcatFunctionWith(final Tokens tokens) {
						return ConcatFunctionArgument.this.nextForConcatFunctionArgument(tokens);
					}

				};
			}

		};
	}

	@Override
	protected IConcatFunctionWith<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new ConcatFunctionWith<T, ET>(tokens) {

			@Override
			protected T nextForConcatFunctionWith(final Tokens tokens) {
				return ConcatFunctionArgument.this.nextForConcatFunctionArgument(tokens);
			}

		};
	}
}