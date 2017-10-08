package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionLastArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET> //
		implements ICaseWhenFunctionLastArgument<T, ET> {

    protected CaseWhenFunctionLastArgument(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForCaseWhenFunctionLastArgument(final Tokens tokens);

	@Override
	protected IExprOperand0<ICaseWhenFunctionEnd<T>, ET> nextForExprOperand(final Tokens tokens) {
		return new ExprOperand0<ICaseWhenFunctionEnd<T>, ET>(tokens) {
			@Override
			protected ICaseWhenFunctionEnd<T> nextForExprOperand0(final Tokens tokens) {
				return new CaseWhenFunctionEnd<T>(tokens) {

					@Override
					protected T nextForCaseWhenFunctionEnd(final Tokens tokens) {
						return CaseWhenFunctionLastArgument.this.nextForCaseWhenFunctionLastArgument(tokens);
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
				return CaseWhenFunctionLastArgument.this.nextForCaseWhenFunctionLastArgument(tokens);
			}

		};
	}
}