package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

abstract class RoundFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IRoundFunctionTo<T>, IExprOperand0<IRoundFunctionTo<T>, ET>, ET> //
		implements IRoundFunctionArgument<T, ET> {

    protected RoundFunctionArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForRoundFunctionArgument(final Tokens tokens);

	@Override
	protected IExprOperand0<IRoundFunctionTo<T>, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<IRoundFunctionTo<T>, ET>(tokens) {

			@Override
			protected IRoundFunctionTo<T> nextForExprOperand0(final Tokens tokens) {
				return new RoundFunctionTo<T>(tokens) {

					@Override
					protected T nextForRoundFunctionTo(final Tokens tokens) {
						return RoundFunctionArgument.this.nextForRoundFunctionArgument(tokens);
					}

				};
			}

		};
	}

	@Override
	protected IRoundFunctionTo<T> nextForSingleOperand(final Tokens tokens) {
		return new RoundFunctionTo<T>(tokens) {

			@Override
			protected T nextForRoundFunctionTo(final Tokens tokens) {
				return RoundFunctionArgument.this.nextForRoundFunctionArgument(tokens);
			}

		};
	}
}