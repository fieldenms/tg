package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedCommon;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class CompletedCommon extends AbstractQueryLink implements ICompletedCommon {

    CompletedCommon(final Tokens queryTokens) {
	super(queryTokens);
    }

    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
	return new EntityResultQueryModel<T>(getTokens().getTokens(), resultType);
    }

    public AggregatedResultQueryModel modelAsAggregate() {
	return new AggregatedResultQueryModel(getTokens().getTokens());
    }
}