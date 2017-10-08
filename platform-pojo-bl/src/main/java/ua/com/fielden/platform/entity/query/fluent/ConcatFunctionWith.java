package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionArgument;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IConcatFunctionWith;

abstract class ConcatFunctionWith<T, ET extends AbstractEntity<?>> //
		extends AbstractQueryLink //
		implements IConcatFunctionWith<T, ET> {

    protected ConcatFunctionWith(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForConcatFunctionWith(final Tokens tokens);

	@Override
	public T end() {
		return nextForConcatFunctionWith(getTokens().endOfFunction());
	}

	@Override
	public IConcatFunctionArgument<T, ET> with() {
		return createConcatFunctionArgument(getTokens());
	}

	private ConcatFunctionArgument<T, ET> createConcatFunctionArgument(final Tokens tokens) {
		return new ConcatFunctionArgument<T, ET>(tokens) {

			protected @Override
			T nextForConcatFunctionArgument(final Tokens tokens) {
				return ConcatFunctionWith.this.nextForConcatFunctionWith(tokens);
			}
		};
	}
}