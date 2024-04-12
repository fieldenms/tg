package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

abstract class RoundFunctionTo<T> //
		extends AbstractQueryLink //
		implements IRoundFunctionTo<T> {

	protected RoundFunctionTo(final EqlSentenceBuilder builder) {
		super(builder);
	}

	protected abstract T nextForRoundFunctionTo(final EqlSentenceBuilder builder);

	@Override
	public T to(final Integer precision) {
		return nextForRoundFunctionTo(builder.to(precision));
	}

}
