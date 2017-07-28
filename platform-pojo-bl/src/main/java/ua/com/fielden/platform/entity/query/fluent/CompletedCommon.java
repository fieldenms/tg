package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedCommon;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class CompletedCommon<ET extends AbstractEntity<?>> extends AbstractQueryLink implements ICompletedCommon<ET> {

    CompletedCommon(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
        return new EntityResultQueryModel<T>(getTokens().getValues(), resultType, getTokens().isYieldAll());
    }

    @Override
    public AggregatedResultQueryModel modelAsAggregate() {
        return new AggregatedResultQueryModel(getTokens().getValues(), getTokens().isYieldAll());
    }
}