package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET> //
		implements ICaseWhenFunctionArgument<T, ET> {

    protected CaseWhenFunctionArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForCaseWhenFunctionArgument(final Tokens tokens);

	@Override
	protected IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>(tokens) {

			@Override
			protected ICaseWhenFunctionWhen<T, ET> nextForExprOperand0(final Tokens tokens) {
				return new CaseWhenFunctionWhen<T, ET>(tokens) {

					@Override
					protected T nextForCaseWhenFunctionEnd(final Tokens tokens) {
						return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument(tokens);
					}

				};
			}

		};
	}

	@Override
	protected ICaseWhenFunctionWhen<T, ET> nextForSingleOperand(final Tokens tokens) {
		return new CaseWhenFunctionWhen<T, ET>(tokens) {

			@Override
			protected T nextForCaseWhenFunctionEnd(final Tokens tokens) {
				return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument(tokens);
			}

		};
	}
}