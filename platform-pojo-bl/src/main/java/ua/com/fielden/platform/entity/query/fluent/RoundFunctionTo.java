package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

abstract class RoundFunctionTo<T> //
		extends AbstractQueryLink //
		implements IRoundFunctionTo<T> {

    protected RoundFunctionTo(final Tokens tokens) {
        super(tokens);
    }
    
	protected abstract T nextForRoundFunctionTo(final Tokens tokens);

	@Override
	public T to(final Integer precision) {
		return nextForRoundFunctionTo(getTokens().to(precision));
	}
}