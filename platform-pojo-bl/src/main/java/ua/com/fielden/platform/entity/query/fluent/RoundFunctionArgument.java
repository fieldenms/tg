package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

abstract class RoundFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends AbstractExprOperand<IRoundFunctionTo<T>, IExprOperand0<IRoundFunctionTo<T>, ET>, ET> //
		implements IRoundFunctionArgument<T, ET> {

	protected abstract T nextForRoundFunctionArgument();

	@Override
	protected IExprOperand0<IRoundFunctionTo<T>, ET> nextForAbstractExprOperand() {
		return new ExprOperand0<IRoundFunctionTo<T>, ET>() {

			@Override
			protected IRoundFunctionTo<T> nextForExprOperand0() {
				return new RoundFunctionTo<T>() {

					@Override
					protected T nextForRoundFunctionTo() {
						return RoundFunctionArgument.this.nextForRoundFunctionArgument();
					}

				};
			}

		};
	}

	@Override
	protected IRoundFunctionTo<T> nextForAbstractSingleOperand() {
		return new RoundFunctionTo<T>() {

			@Override
			protected T nextForRoundFunctionTo() {
				return RoundFunctionArgument.this.nextForRoundFunctionArgument();
			}

		};
	}
}