package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IExprOperand0;

abstract class ConcatFunctionArgument<T, ET extends AbstractEntity<?>> //
		extends ExprOperand<IConcatFunctionWith<T, ET>, IExprOperand0<IConcatFunctionWith<T, ET>, ET>, ET> //
		implements IConcatFunctionArgument<T, ET> {

	protected ConcatFunctionArgument(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForConcatFunctionArgument(final EqlSentenceBuilder builder);

	@Override
	protected IExprOperand0<IConcatFunctionWith<T, ET>, ET> nextForExprOperand(final EqlSentenceBuilder builder) {
		return new ExprOperand0<IConcatFunctionWith<T, ET>, ET>(builder) {

			@Override
			protected IConcatFunctionWith<T, ET> nextForExprOperand0(final EqlSentenceBuilder builder) {
				return new ConcatFunctionWith<T, ET>(builder) {

					@Override
					protected T nextForConcatFunctionWith(final EqlSentenceBuilder builder) {
						return ConcatFunctionArgument.this.nextForConcatFunctionArgument(builder);
					}

				};
			}

		};
	}

	@Override
	protected IConcatFunctionWith<T, ET> nextForSingleOperand(final EqlSentenceBuilder builder) {
		return new ConcatFunctionWith<T, ET>(builder) {

			@Override
			protected T nextForConcatFunctionWith(final EqlSentenceBuilder builder) {
				return ConcatFunctionArgument.this.nextForConcatFunctionArgument(builder);
			}

		};
	}

}
