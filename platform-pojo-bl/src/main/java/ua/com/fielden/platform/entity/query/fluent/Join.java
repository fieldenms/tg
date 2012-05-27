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
    public <T extends AbstractEntity<?>> IJoinAlias join(final Class<T> entityType) {
	return new JoinAlias(getTokens().innerJoin(entityType));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias leftJoin(final Class<T> entityType) {
	return new JoinAlias(getTokens().leftJoin(entityType));
    }

    @Override
    public IJoinAlias join(final AggregatedResultQueryModel model) {
	return new JoinAlias(getTokens().innerJoin(model));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias join(final EntityResultQueryModel<T> model) {
	return new JoinAlias(getTokens().innerJoin(model));
    }

    @Override
    public IJoinAlias leftJoin(final AggregatedResultQueryModel model) {
	return new JoinAlias(getTokens().leftJoin(model));
    }

    @Override
    public <T extends AbstractEntity<?>> IJoinAlias leftJoin(final EntityResultQueryModel<T> model) {
	return new JoinAlias(getTokens().leftJoin(model));
    }

}
