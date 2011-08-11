package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractCloseGroup;

abstract class AbstractCloseGroup<T> extends AbstractQueryLink implements IAbstractCloseGroup<T> {

    protected AbstractCloseGroup(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    abstract T createCloseGroup(final QueryTokens queryTokens);

    @Override
    public T end() {
	return createCloseGroup(this.getTokens().closeParenthesis());
    }
}
