package ua.com.fielden.platform.equery;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.equery.interfaces.IMain.ICompletedAndOrdered;

class CompletedAndOrdered extends AbstractQueryLink implements ICompletedAndOrdered {

    CompletedAndOrdered(final QueryTokens queryTokens) {
	super(queryTokens);
    }

    @Override
    public <T extends AbstractEntity> IQueryOrderedModel<T> model() {
	return new QueryModel<T>(getTokens());
    }

    @Override
    public <T extends AbstractEntity> IQueryOrderedModel<T> model(final Class<T> resultType) {
	return new QueryModel<T>(getTokens(), resultType);
    }

    @Override
    public ICompletedAndOrdered orderBy(final String... otherProperties) {
	return new CompletedAndOrdered(this.getTokens().orderBy(otherProperties));
    }
}