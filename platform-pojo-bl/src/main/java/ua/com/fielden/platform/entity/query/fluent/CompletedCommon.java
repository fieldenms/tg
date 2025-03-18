package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompletedCommon;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class CompletedCommon<ET extends AbstractEntity<?>> //
        extends AbstractQueryLink //
        implements ICompletedCommon<ET> {

    protected CompletedCommon(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public <T extends AbstractEntity<?>> EntityResultQueryModel<T> modelAsEntity(final Class<T> resultType) {
        return new EntityResultQueryModel<T>(builder.modelAsEntity(resultType).getTokenSource(), resultType, builder.isYieldAll());
    }

    @Override
    public AggregatedResultQueryModel modelAsAggregate() {
        return new AggregatedResultQueryModel(builder.modelAsAggregate().getTokenSource(), builder.isYieldAll());
    }

}
