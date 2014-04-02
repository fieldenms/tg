package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoin;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IJoinAlias;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;

class Join<ET extends AbstractEntity<?>> extends PlainJoin<ET> implements IJoin<ET> {

    Join(final Tokens queryTokens) {
        super(queryTokens);
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> join(final Class<T> entityType) {
        return new JoinAlias<ET>(getTokens().innerJoin(entityType));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> leftJoin(final Class<T> entityType) {
        return new JoinAlias<ET>(getTokens().leftJoin(entityType));
    }

    @Override
    public IJoinAlias<ET> join(final AggregatedResultQueryModel model) {
        return new JoinAlias<ET>(getTokens().innerJoin(model));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> join(final EntityResultQueryModel<T> model) {
        return new JoinAlias<ET>(getTokens().innerJoin(model));
    }

    @Override
    public IJoinAlias<ET> leftJoin(final AggregatedResultQueryModel model) {
        return new JoinAlias<ET>(getTokens().leftJoin(model));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias<ET> leftJoin(final EntityResultQueryModel<T> model) {
        return new JoinAlias<ET>(getTokens().leftJoin(model));
    }
}