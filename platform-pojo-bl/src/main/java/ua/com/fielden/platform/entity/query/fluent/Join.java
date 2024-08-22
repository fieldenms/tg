package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinAlias;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class Join<ET extends AbstractEntity<?>> extends PlainJoin<ET> implements IJoin<ET> {

    protected Join(final EqlSentenceBuilder builder) {
        super(builder);
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> join(final Class<T> entityType) {
        return new JoinAlias<ET>(builder.innerJoin(entityType));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> leftJoin(final Class<T> entityType) {
        return new JoinAlias<ET>(builder.leftJoin(entityType));
    }

    @Override
    public IJoinAlias<ET> join(final AggregatedResultQueryModel model) {
        return new JoinAlias<ET>(builder.innerJoin(model));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> join(final EntityResultQueryModel<T> model) {
        return new JoinAlias<ET>(builder.innerJoin(model));
    }

    @Override
    public IJoinAlias<ET> leftJoin(final AggregatedResultQueryModel model) {
        return new JoinAlias<ET>(builder.leftJoin(model));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> leftJoin(final EntityResultQueryModel<T> model) {
        return new JoinAlias<ET>(builder.leftJoin(model));
    }

}
