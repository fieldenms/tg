package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractOpenGroup;

abstract class AbstractOpenGroup<T> extends AbstractQueryLink implements IAbstractOpenGroup<T> {

    protected AbstractOpenGroup(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    abstract T createOpenGroup(final QueryTokens queryTokens);

    @Override
    public T begin() {
	return createOpenGroup(this.getTokens().openParenthesis(false));
    }

    @Override
    public T notBegin() {
	return createOpenGroup(this.getTokens().openParenthesis(true));
    }
}
