package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;

abstract class ConcatFunctionWith<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IConcatFunctionWith<T, ET> {

	protected ConcatFunctionWith(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForConcatFunctionWith(final EqlSentenceBuilder builder);

	@Override
	public T end() {
		return nextForConcatFunctionWith(builder.endOfFunction());
	}

	@Override
	public IConcatFunctionArgument<T, ET> with() {
		return createConcatFunctionArgument(builder.with());
	}

	private ConcatFunctionArgument<T, ET> createConcatFunctionArgument(final EqlSentenceBuilder builder) {
		return new ConcatFunctionArgument<T, ET>(builder) {

			protected @Override
			T nextForConcatFunctionArgument(final EqlSentenceBuilder builder) {
				return ConcatFunctionWith.this.nextForConcatFunctionWith(builder);
			}
		};
	}

}
