package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.equery.interfaces.IAbstract.IAbstractLogicalCondition;

abstract class AbstractLogicalCondition<T> extends AbstractQueryLink implements IAbstractLogicalCondition<T> {

    protected AbstractLogicalCondition(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    abstract T createImplicitCondition(final QueryTokens queryTokens);

    @Override
    public T and() {
	return createImplicitCondition(this.getTokens().and());
    }

    @Override
    public T or() {
	return createImplicitCondition(this.getTokens().or());
    }
}
