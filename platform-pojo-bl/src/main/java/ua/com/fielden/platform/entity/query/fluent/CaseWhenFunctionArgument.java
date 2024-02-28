package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<ICaseWhenFunctionWhen<T, ET>, IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>, ET> //
		implements ICaseWhenFunctionArgument<T, ET> {

	protected CaseWhenFunctionArgument(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForCaseWhenFunctionArgument(final EqlSentenceBuilder builder);

	@Override
	protected IExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
		return new ExprOperand0<ICaseWhenFunctionWhen<T, ET>, ET>(builder) {

			@Override
			protected ICaseWhenFunctionWhen<T, ET> nextForExprOperand0(final EqlSentenceBuilder builder) {
				return new CaseWhenFunctionWhen<T, ET>(builder) {

					@Override
					protected T nextForCaseWhenFunctionEnd(final EqlSentenceBuilder builder) {
						return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument(builder);
					}

				};
			}

		};
	}

	@Override
	protected ICaseWhenFunctionWhen<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
		return new CaseWhenFunctionWhen<T, ET>(builder) {

			@Override
			protected T nextForCaseWhenFunctionEnd(final EqlSentenceBuilder builder) {
				return CaseWhenFunctionArgument.this.nextForCaseWhenFunctionArgument(builder);
			}

		};
	}

}
