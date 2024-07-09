package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionEnd;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionLastArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICaseWhenFunctionWhen;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class CaseWhenFunctionLastArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<ICaseWhenFunctionEnd<T>, IExprOperand0<ICaseWhenFunctionEnd<T>, ET>, ET> //
		implements ICaseWhenFunctionLastArgument<T, ET> {

	protected CaseWhenFunctionLastArgument(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForCaseWhenFunctionLastArgument(final EqlSentenceBuilder builder);

	@Override
	protected IExprOperand0<ICaseWhenFunctionEnd<T>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
		return new ExprOperand0<ICaseWhenFunctionEnd<T>, ET>(builder) {
			@Override
			protected ICaseWhenFunctionEnd<T> nextForExprOperand0(final EqlSentenceBuilder builder) {
				return new CaseWhenFunctionEnd<T>(builder) {

					@Override
					protected T nextForCaseWhenFunctionEnd(final EqlSentenceBuilder builder) {
						return CaseWhenFunctionLastArgument.this.nextForCaseWhenFunctionLastArgument(builder);
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
				return CaseWhenFunctionLastArgument.this.nextForCaseWhenFunctionLastArgument(builder);
			}

		};
	}

}
