package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IRoundFunctionTo;

public class RoundFunctionTo<T> extends AbstractQueryLink implements IRoundFunctionTo<T> {
    T parent;

    RoundFunctionTo(final Tokens queryTokens, final T parent) {
	super(queryTokens);
	this.parent = parent;
    }
    @Override
    public T to(final Integer precision) {
	((AbstractQueryLink) parent).setTokens(getTokens().to(precision));
	return parent;
    }
}
